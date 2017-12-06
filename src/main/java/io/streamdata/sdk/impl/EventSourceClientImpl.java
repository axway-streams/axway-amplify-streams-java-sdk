package io.streamdata.sdk.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonPatch;
import io.streamdata.sdk.EventSourceClient;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class EventSourceClientImpl implements EventSourceClient {


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
    private AtomicReference<JsonNode> currentSnapshot = new AtomicReference<>();

    private EventSource eventSource;


    private final StringBuffer url;
    private boolean incrementalCache = true;

    /**
     * Build the url to be called eventually
     *
     * @param apiUrl the api URL
     * @param appKey the key
     * @throws URISyntaxException when the polling URL is not OK
     */
    public EventSourceClientImpl(String apiUrl, String appKey) throws URISyntaxException {
        checkNotNull(apiUrl, "apiUrl cannot be null");
        checkNotNull(appKey, "appKey cannot be null");

        // check the url
        URI uri = new URI(apiUrl);

        String queryParamSeparator = (uri.getQuery() == null || uri.getQuery().isEmpty()) ? "?" : "&";

        this.url = new StringBuffer(SD_PROXY_URL)
                .append(apiUrl)
                .append(queryParamSeparator)
                .append("X-Sd-Token=")
                .append(appKey);
    }

    public EventSourceClient addHeader(String name, String value) {

        try {
            this.url.append('&')
                    .append("X-Sd-Header=")
                    .append(URLEncoder.encode(name, "UTF-8"))
                    .append(':')
                    .append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // when the encoding our self... no need to throw a check exception
            throw new IllegalArgumentException(e);
        }
        return this;
    }

    @Override
    public EventSourceClient incrementalCache(boolean enableIncrementalCache) {
        this.incrementalCache = enableIncrementalCache;
        return this;
    }

    @Override
    public EventSourceClient onOpen(Runnable callback) {
        this.onOpenCallback = callback;
        return this;
    }

    @Override
    public EventSourceClient onSnapshot(Consumer<JsonNode> snaphot) {
        this.onDataCallback = snaphot;
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
    public JsonNode getCurrentSnapshot() {
        return this.currentSnapshot.get();
    }


    @Override
    public EventSourceClient open() {

        checkNotNull(this.onDataCallback, "You must call onSnapshot() with a non-null callback before calling open()");
        if (incrementalCache) {
            checkNotNull(this.onPatchCallback, "You must call onPatch() with a non-null callback before calling open()");
        }
        checkArgument(this.eventSource == null, "You cannot call open() on an already opened event source");

        try {

            Client webClient = ClientBuilder.newBuilder()
                    .register(SseFeature.class)
                    .register((Feature) context -> {
                        context.register((ClientRequestFilter) requestContext -> {
                            requestContext.getHeaders().get("Accept").clear();
                            requestContext.getHeaders().get("Accept").add(this.incrementalCache ? SseFeature.SERVER_SENT_EVENTS : MediaType.APPLICATION_JSON);
                        });
                        return true;
                    })
                    .build();

            WebTarget target = webClient.target(EventSourceClientImpl.this.url.toString());


            this.eventSource = new EventSource(target) {

                @Override
                public void onEvent(InboundEvent inboundEvent) {

                    // get data from the source
                    String eventName = inboundEvent.getName();
                    String eventData = new String(inboundEvent.getRawData());

                    switch (eventName) {
                        case "data":
                            LOGGER.debug("Receiving data {} ", eventData);
                            try {
                                // read the data
                                final JsonNode data = jsonObjectMapper.readTree(eventData);

                                // set it in a thread-safe fashion
                                currentSnapshot.set(data);

                                // notify observer
                                onDataCallback.accept(data);
                            } catch (IOException e) {
                                // notify consumer
                                onFailureCallback.accept(e);
                            }
                            break;

                        case "patch":
                            LOGGER.debug("Receiving patch {} ", eventData);
                            try {
                                // read the patch
                                JsonNode lastPatch = jsonObjectMapper.readTree(eventData);

                                // apply the patch to the last know data value
                                JsonNode data = JsonPatch.apply(lastPatch, currentSnapshot.get());

                                // set it in a thread safe and atomic fashion
                                currentSnapshot.set(data);

                                // notify observer
                                onPatchCallback.accept(lastPatch);

                            } catch (IOException e) {
                                onFailureCallback.accept(e);
                            }
                            break;

                        case "error":
                            LOGGER.debug("Receiving error {} ", eventData);
                            onErrorCallback.accept(eventData);
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
            onFailureCallback.accept(e);
            this.close();
            System.exit(1);
        }

        return this;


    }


}
