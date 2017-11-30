package io.streamdata.jdk.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonPatch;
import com.google.common.base.Preconditions;
import io.streamdata.jdk.EventSourceClient;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class EventSourceClientImpl extends AbstractEventStreamer<EventSourceClient> implements EventSourceClient {

    // define a slf4j logger
    private static final Logger LOGGER = LoggerFactory.getLogger(EventSourceClientImpl.class);


    // The polling URL
    private Runnable onOpenCallback;
    private Consumer<JsonNode> onDataCallback;
    private Consumer<JsonNode> onPatchCallback;
    private Consumer<String> onErrorCallback = err -> LOGGER.error("A streamdata error has been sent from SSE : {}", err);
    private Consumer<Throwable> onFailureCallback = t -> LOGGER.error("An error occured while processing event", t);

    // jackson objectMapper to parse Json content
    private final ObjectMapper jsonObjectMapper = new ObjectMapper();

    // local storage of the data
    private AtomicReference<JsonNode> currentData = new AtomicReference<>();
    private AtomicReference<JsonNode> lastPatch = new AtomicReference<>();

    private final Client webClient = ClientBuilder.newBuilder().register(SseFeature.class).build();
    private EventSource eventSource;


    public EventSourceClientImpl(String apiUrl, String appKey) throws URISyntaxException {
        super(apiUrl, appKey);
    }

    @Override
    public EventSourceClient onOpen(Runnable callback) {
        this.onOpenCallback = callback;
        return this;
    }

    @Override
    public EventSourceClient onData(Consumer<JsonNode> callback) {
        this.onDataCallback = callback;
        return this;
    }

    @Override
    public EventSourceClient onPatch(Consumer<JsonNode> callback) {
        this.onPatchCallback = callback;
        return this;
    }

    @Override
    public EventSourceClient onError(Consumer<String> callback) {
        this.onErrorCallback = callback;
        return this;
    }

    @Override
    public EventSourceClient onException(Consumer<Throwable> callback) {
        this.onFailureCallback = callback;
        return this;
    }

    @Override
    public void close() {
        if (this.eventSource != null) {
            this.eventSource.close();
            this.eventSource = null;
        }
    }

    @Override
    public JsonNode getCurrentData() {
        return this.currentData.get();
    }

    @Override
    public JsonNode getLastPatch() {
        return this.lastPatch.get();
    }

    @Override
    public Future<?> open() {

        Preconditions.checkNotNull(this.onDataCallback, "You must call onData() with a non-null callback before calling open()");
        Preconditions.checkNotNull(this.onPatchCallback, "You must call onPatch() with a non-null callback before calling open()");
        Preconditions.checkArgument(this.eventSource == null, "You cannot call open() on an already opened event source");


        return Executors.newSingleThreadExecutor().submit(() -> {
            try {

                this.eventSource = new EventSource(EventSourceClientImpl.this.webClient.target(EventSourceClientImpl.this.url.toString())) {

                    @Override
                    public void onEvent(InboundEvent inboundEvent) {

                        // get data from the source
                        String eventName = inboundEvent.getName();
                        String eventData = new String(inboundEvent.getRawData());

                        switch (eventName) {
                            case "data":
                                try {
                                    // read the data
                                    final JsonNode data = jsonObjectMapper.readTree(eventData);
                                    // set it in a thread-safe fashion
                                    EventSourceClientImpl.this.currentData.set(data);
                                    // notify observer
                                    EventSourceClientImpl.this.onDataCallback.accept(data);
                                } catch (IOException e) {
                                    // notify consumer
                                    EventSourceClientImpl.this.onFailureCallback.accept(e);
                                }
                                break;

                            case "patch":
                                try {
                                    // read the patch
                                    JsonNode lastPatch1 = jsonObjectMapper.readTree(eventData);

                                    // apply the patch to the last know data value
                                    JsonNode data = JsonPatch.apply(lastPatch1, currentData.get());

                                    // set it in a thread safe and atomic fashion
                                    synchronized (EventSourceClientImpl.this.jsonObjectMapper) {
                                        EventSourceClientImpl.this.lastPatch.set(lastPatch1);
                                        EventSourceClientImpl.this.currentData.set(data);

                                    }

                                    // notify observer
                                    EventSourceClientImpl.this.onPatchCallback.accept(lastPatch1);

                                } catch (IOException e) {
                                    EventSourceClientImpl.this.onFailureCallback.accept(e);
                                }
                                break;

                            case "error":
                                EventSourceClientImpl.this.onErrorCallback.accept(eventData);
                                break;

                            default:
                                LOGGER.warn("Unhandled event received with name '{}' and data : {}", eventName, eventData);

                        }

                    }

                };
                // it is open... we are excepting thing to happen from now
                if (this.onOpenCallback != null)
                    this.onOpenCallback.run();
            } catch (Exception e) {
                EventSourceClientImpl.this.onFailureCallback.accept(e);
                this.close();
                System.exit(1);
            }
        });


    }


}
