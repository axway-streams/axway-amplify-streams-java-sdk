package io.streamdata.jdk.impl;

import io.streamdata.jdk.Event;
import io.streamdata.jdk.EventSourceClient;
import io.streamdata.jdk.StreamApiClient;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.net.URISyntaxException;

public class StreamApiClientImpl implements StreamApiClient {


    EventSourceClient client;

    /**
     * Build the url to be called eventually
     *
     * @param apiUrl the api URL
     * @param appKey the key
     * @throws URISyntaxException when the polling URL is not OK
     */
    public StreamApiClientImpl(String apiUrl, String appKey) throws URISyntaxException {
        this.client = EventSourceClient.createEventSource(apiUrl, appKey);
    }

    @Override
    public StreamApiClient addStreamHeader(String name, String value) {
        this.client.addHeader(name, value);
        return this;
    }

    @Override
    public Observable<Event> toObservable() {

        final PublishSubject<Event> subject = PublishSubject.create();


        subject.doOnUnsubscribe(this.client::close);
        this.client.onData(data -> subject.onNext(Event.forData(data)));
        this.client.onPatch(patch -> subject.onNext(Event.forPatch(patch)));
        this.client.onError(error -> subject.onNext(Event.forError(error)));
        this.client.onException(subject::onError);

        this.client.open();

        return subject;
    }

}
