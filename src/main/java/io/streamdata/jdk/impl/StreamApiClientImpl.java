package io.streamdata.jdk.impl;

import io.streamdata.jdk.Event;
import io.streamdata.jdk.EventSourceClient;
import io.streamdata.jdk.StreamApiClient;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

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
    public StreamApiClient addStreamHeader(String name, String value) {
        this.eventSourceClient.addHeader(name, value);
        return this;
    }

    @Override
    public Observable<Event> toObservable() {

        final PublishSubject<Event> subject = PublishSubject.create();
        subject.observeOn(Schedulers.io());

        this.eventSourceClient.onSnapshot(data -> subject.onNext(Event.forSnapshot(data)));
        this.eventSourceClient.onPatch(patch -> subject.onNext(Event.forPatch(this.eventSourceClient.getCurrentData(), patch)));
        this.eventSourceClient.onError(error -> subject.onNext(Event.forError(error)));
        this.eventSourceClient.onException(subject::onError);

        this.eventSourceClient.open();

        return subject;
    }

    @Override
    public void open() {
        this.eventSourceClient.open();
    }

    @Override
    public void close() {
        this.eventSourceClient.close();
    }

}
