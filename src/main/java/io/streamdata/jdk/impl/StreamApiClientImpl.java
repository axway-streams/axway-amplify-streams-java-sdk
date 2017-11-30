package io.streamdata.jdk.impl;

import io.reactivex.Observable;
import io.streamdata.jdk.Event;
import io.streamdata.jdk.StreamApiClient;

import java.net.URISyntaxException;

public class StreamApiClientImpl extends AbstractEventStreamer<StreamApiClient> implements StreamApiClient {
    /**
     * Build the url to be called eventually
     *
     * @param apiUrl the api URL
     * @param appKey the key
     * @throws URISyntaxException when the polling URL is not OK
     */
    public StreamApiClientImpl(String apiUrl, String appKey) throws URISyntaxException {
        super(apiUrl, appKey);
    }

    @Override
    public Observable<Event> toObservable() {

        return null;
    }
}
