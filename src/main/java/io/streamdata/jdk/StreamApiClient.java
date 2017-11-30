package io.streamdata.jdk;

import io.reactivex.Observable;
import io.streamdata.jdk.impl.StreamApiClientImpl;

import java.net.URISyntaxException;

public interface StreamApiClient extends ApiStreamer<StreamApiClient> {

    static StreamApiClient createEventStream(String apiUrl, String appKey) throws URISyntaxException {
        return new StreamApiClientImpl(apiUrl, appKey);
    }


    Observable<Event> toObservable();

}
