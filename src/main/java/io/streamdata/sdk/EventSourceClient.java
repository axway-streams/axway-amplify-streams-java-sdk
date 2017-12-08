package io.streamdata.sdk;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.function.Consumer;

/**
 * This class allows to call the proxy and get the result of polling.
 */
public interface EventSourceClient {

    String SD_PROXY_URL = "https://streamdata.motwin.net/";


    /**
     * Add a header to the polling request (those header will be passed to the request when SD.io will poll the API)
     *
     * @param name  name of the header
     * @param value value of the header
     * @return this client instance for nice fluent api call
     */
    EventSourceClient addHeader(String name, String value);


    /**
     * <p>Allow to enable or disable incremental cache. <b>By default incremental cache is enabled</b> thus the following happens : a Snapshot is sent back to the user followed by successive patches of this snapshot.</p>
     * <p>If set to false a snapshot will be sent every time, no patch is sent. This means that {@link RxJavaEventSourceClient.Event#getPatch()} will return null <b>Use this only for low frequency polling</b></p>
     * <p>Behind the scene it adds the header <code>text/event-stream</code> for patches or <code>application/json</code> for non-incremental cache</p>
     *
     * @param enableIncrementalCache a boolean to allow incremental cache (default : true)
     */
    EventSourceClient incrementalCache(boolean enableIncrementalCache);

    /**
     * Sets a optionnal callback to be called after the event source has been successfully started.
     *
     * @param onOpen the callback
     * @return this client instance for nice fluent api call
     */
    EventSourceClient onOpen(Runnable onOpen);

    /**
     * Sets a optionnal callback to be called after the event source has been closed
     *
     * @param onClose the callback
     * @return this client instance for nice fluent api call
     */
    EventSourceClient onClose(Runnable onClose);

    /**
     * Sets a callback to be called after streamdata sends after the first time the API is polled.
     * <b>This callback must be set before calling {@link #open()}</b>
     *
     * @param snaphot the callback (can be a lambda expression)
     * @return this client instance for nice fluent api call
     */
    EventSourceClient onSnapshot(Consumer<JsonNode> snaphot);

    /**
     * Sets a callback to be called every time streamdata pushes a patch. The patch is applied behind the scenes and can be accessed in a thread safe fashion using {@link #getCurrentSnapshot()}
     * * <b>This callback must be set before calling {@link #open()}</b>
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
    EventSourceClient onError(Consumer<JsonNode> callback);

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
     * Get the snapshot (initial or after a patch is received and applied)
     *
     * @return the most fresh snapshot available
     */
    JsonNode getCurrentSnapshot();


    /**
     * Opens the connections with streamdata proxy that will poll data for you.
     * {@link #onSnapshot(Consumer)} must have called before and {@link #onPatch(Consumer)} is incremental cache is on (default is yes unless you call {@link #incrementalCache(boolean)} with false.
     *
     * @return a future to get hints on the thread status
     */
    EventSourceClient open();

    /**
     * Closes the event source gracefully
     */
    void close();

}
