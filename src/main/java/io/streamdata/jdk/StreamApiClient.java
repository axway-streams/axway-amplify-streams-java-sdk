package io.streamdata.jdk;

import io.streamdata.jdk.impl.StreamApiClientImpl;
import rx.Observable;

import java.net.URISyntaxException;

public interface StreamApiClient {

    static StreamApiClient createEventStream(String apiUrl, String appKey) throws URISyntaxException {
        return new StreamApiClientImpl(apiUrl, appKey);
    }

    /**
     * Add a header to the polling request (those header will be passed to the request when SD.io will poll the API)
     *
     * @param name  name of the header
     * @param value value of the header
     * @return this client instance for nice fluent api call
     */
    StreamApiClient addStreamHeader(String name, String value);

    /**
     * Expose the arrival of data
     *
     * @return an observable that triggers realtime data
     */
    Observable<Event> toObservable();

    void open();

    void close();

}
