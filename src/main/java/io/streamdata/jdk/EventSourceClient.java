package io.streamdata.jdk;

import com.fasterxml.jackson.databind.JsonNode;
import io.streamdata.jdk.impl.EventSourceClientImpl;

import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * This class allows to call the proxy and get the result of polling.
 */
public interface EventSourceClient {

    String POLLER_URL = "https://streamdata.motwin.net/";

    /**
     * Create an event source for a apiUrl
     *
     * @param apiUrl the url to be polled
     * @param appKey the app key that will be passed to the proxy
     * @return a client to be
     * @throws URISyntaxException if the URL to poll is not a valid URL
     */
    default EventSourceClient createEventSource(String apiUrl, String appKey) throws URISyntaxException {
        return new EventSourceClientImpl(apiUrl, appKey);
    }

    /**
     * Add a header to the polling request (those header will be passed to the request when SD.io will poll the API)
     *
     * @param name  name of the header
     * @param value value of the header
     * @return this client instance for nice fluent api call
     */
    EventSourceClient addHeader(String name, String value);

    /**
     * Sets a optionnal callback to be called after the event source has been successfully started.
     *
     * @param onOpen the callback
     * @return this client instance for nice fluent api call
     */
    EventSourceClient onOpen(Runnable onOpen);

    /**
     * Sets a callback to be called after streamdata sends after the first time the API is polled.
     * <b>This method must be called before calling {@link #open()}</b>
     *
     * @param onData the callback (can be a lambda expression)
     * @return this client instance for nice fluent api call
     */
    EventSourceClient onData(Consumer<JsonNode> onData);

    /**
     * Sets a callback to be called every time streamdata pushes a patch. The patch is applied behind the scenes and can be accessed in a thread safe fashion using {@link #getCurrentData()}
     * * <b>This method must be called before calling {@link #open()}</b>
     *
     * @param onOpen the callback
     * @return this client instance for nice fluent api call
     */
    EventSourceClient onPatch(Consumer<JsonNode> onOpen);

    /**
     * Sets a callback to be called every time streamdata pushes an error. By default if no action is made except logging.
     *
     * @param callback the callback
     * @return this client instance for nice fluent api call
     */
    EventSourceClient onError(Consumer<String> callback);

    /**
     * Sets a callback that is called when a exception is raised :
     * <ul>
     * <li>The data cannot be parsed</li>
     * <li>The patch cannot be parsed</li>
     * <li>The event source fails to start</li>
     * </ul>
     *
     * @param callback the callback
     * @return this client instance for nice fluent api call
     */
    EventSourceClient onException(Consumer<Throwable> callback);

    /**
     * Get the data (initial or after a patch is received and applied)
     *
     * @return the most fresh data available
     */
    JsonNode getCurrentData();


    /**
     * Returns the last patch that has been received
     *
     * @return the last patch received or null
     */
    JsonNode getLastPatch();

    /**
     * Opens the connections with streamdata proxy that will poll data for you. {@link #onData(Consumer)} and {@link #onPatch(Consumer)} must have called before.
     *
     * @return a future to get hints on the thread status
     */
    Future<?> open();

    /**
     * Closes the event source gracefully
     */
    void close();

}
