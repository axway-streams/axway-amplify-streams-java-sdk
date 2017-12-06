package io.streamdata.sdk.impl;

import com.google.common.base.Preconditions;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.streamdata.sdk.EventSourceClient;
import io.streamdata.sdk.RxJavaEventSourceClient;

import java.net.URISyntaxException;

public class RxJavaEventSourceClientImpl implements RxJavaEventSourceClient {


    private EventSourceClient eventSourceClient;

    public RxJavaEventSourceClientImpl(String apiUrl, String appKey) throws URISyntaxException {
        this.eventSourceClient = new EventSourceClientImpl(apiUrl, appKey);
    }

    @Override
    public RxJavaEventSourceClient addHeader(String name, String value) {
        this.eventSourceClient.addHeader(name, value);
        return this;
    }

    @Override
    public RxJavaEventSourceClient incrementalCache(boolean enableIncrementalCache) {
        this.eventSourceClient.incrementalCache(enableIncrementalCache);
        return this;
    }

    @Override
    public Flowable<Event> toFlowable() {
        return toFlowable(Schedulers.computation());
    }

    @Override
    public Flowable<Event> toFlowable(final Scheduler scheduler) {

        Preconditions.checkNotNull(scheduler, "You must provide a Schduler or call toFlowable() to get a default one");

        return Flowable.<Event>create(emitter -> {

            this.eventSourceClient.onSnapshot(data -> emitter.onNext(Event.forSnapshot(data)));
            this.eventSourceClient.onPatch(patch -> emitter.onNext(Event.forPatch(this.eventSourceClient.getCurrentSnapshot(), patch)));
            this.eventSourceClient.onError(error -> emitter.onNext(Event.forError(error)));
            this.eventSourceClient.onException(emitter::onError);

            emitter.setCancellable(this.eventSourceClient::close);

            this.eventSourceClient.open();

        }, BackpressureStrategy.DROP)
                .observeOn(scheduler);


    }


}
