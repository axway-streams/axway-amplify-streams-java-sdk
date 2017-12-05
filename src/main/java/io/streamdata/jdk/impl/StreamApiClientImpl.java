package io.streamdata.jdk.impl;

import io.streamdata.jdk.Event;
import io.streamdata.jdk.EventSourceClient;
import io.streamdata.jdk.StreamApiClient;
import rx.Emitter;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.net.URISyntaxException;

public class StreamApiClientImpl implements StreamApiClient {


    private EventSourceClient eventSourceClient;

    /**
     * Build the url to be called eventually
     *
     * @param apiUrl the api URL
     * @param appKey the key
     * @throws URISyntaxException when the polling URL is not OK
     */
    public StreamApiClientImpl(String apiUrl, String appKey) throws URISyntaxException {
        this.eventSourceClient = EventSourceClient.createEventSource(apiUrl, appKey);
    }

    @Override
    public StreamApiClient addHeader(String name, String value) {
        this.eventSourceClient.addHeader(name, value);
        return this;
    }

    @Override
    public Observable<Event> toObservable(final Scheduler scheduler) {

        return Observable.<Event>create(emitter -> {

            this.eventSourceClient.onSnapshot(data -> emitter.onNext(Event.forSnapshot(data)));
            this.eventSourceClient.onPatch(patch -> emitter.onNext(Event.forPatch(this.eventSourceClient.getCurrentData(), patch)));
            this.eventSourceClient.onError(error -> emitter.onNext(Event.forError(error)));
            this.eventSourceClient.onException(emitter::onError);

            emitter.setCancellation(this.eventSourceClient::close);

            this.eventSourceClient.open();

        }, Emitter.BackpressureMode.DROP)
                .observeOn(scheduler == null ? Schedulers.computation() : scheduler);


    }


}
