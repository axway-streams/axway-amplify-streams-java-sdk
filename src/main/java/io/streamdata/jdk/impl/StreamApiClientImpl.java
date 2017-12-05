package io.streamdata.jdk.impl;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.streamdata.jdk.Event;
import io.streamdata.jdk.EventSourceClient;
import io.streamdata.jdk.StreamApiClient;

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
    public StreamApiClient incrementalCache(boolean enableIncrementalCache) {
        this.eventSourceClient.incrementalCache(enableIncrementalCache);
        return this;
    }

    @Override
    public Flowable<Event> toObservable(final Scheduler scheduler) {

        return Flowable.<Event>create(emitter -> {

            this.eventSourceClient.onSnapshot(data -> emitter.onNext(Event.forSnapshot(data)));
            this.eventSourceClient.onPatch(patch -> emitter.onNext(Event.forPatch(this.eventSourceClient.getCurrentData(), patch)));
            this.eventSourceClient.onError(error -> emitter.onNext(Event.forError(error)));
            this.eventSourceClient.onException(emitter::onError);

            emitter.setCancellable(this.eventSourceClient::close);

            this.eventSourceClient.open();

        }, BackpressureStrategy.DROP)
                .observeOn(scheduler == null ? Schedulers.computation() : scheduler);


    }


}
