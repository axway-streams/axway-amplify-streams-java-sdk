package io.streamdata.sdk;

import io.streamdata.sdk.impl.EventSourceClientImpl;
import io.streamdata.sdk.impl.RxJavaEventSourceClientImpl;

import java.net.URISyntaxException;

/**
 * Entrypoint interface to
 */
public interface StreamdataClient {

    /**
     * Create an event source client for an apiUrl
     *
     * @param apiUrl the url to be polled
     * @param appKey the app key that will be passed to the proxy
     * @return a client to be
     * @throws URISyntaxException if the URL to poll is not a valid URL
     */
    static EventSourceClient createClient(String apiUrl, String appKey) throws URISyntaxException {
        return new EventSourceClientImpl(apiUrl, appKey);
    }

    /**
     * Create an event source client for an apiUrl
     *
     * @param apiUrl the url to be polled
     * @param appKey the app key that will be passed to the proxy
     * @return a client to be
     * @throws URISyntaxException if the URL to poll is not a valid URL
     */
    static RxJavaEventSourceClient createRxJavaClient(String apiUrl, String appKey) throws URISyntaxException {
        return new RxJavaEventSourceClientImpl(apiUrl, appKey);
    }
}
