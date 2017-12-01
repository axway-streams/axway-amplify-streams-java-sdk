package io.streamdata.demo;

import com.flipkart.zjsonpatch.JsonPatch;
import io.streamdata.jdk.Event;
import io.streamdata.jdk.EventSourceClient;
import io.streamdata.jdk.StreamApiClient;
import rx.Subscription;

import java.net.URISyntaxException;

public class Main {

    public static void main(String... args) throws URISyntaxException, InterruptedException {

        final String appKey = "ODZjZDQ5MDYtYzZkYS00NTQwLWI0ZDctMGZlYzU2N2JlYmY3";
        final String apiURL = "http://stockmarket.streamdata.io/prices";

/*
* Using event source client
 */
        EventSourceClient eventSource = EventSourceClient.createEventSource(apiURL, appKey);
        eventSource
                .addHeader("X-MYAPI-HEADER", "Polled_By_SD.io")
                .onData(data -> System.out.println("INITIAL DATA " + data))
                .onPatch(patch -> System.out.println("PATCH " + patch + " DATA UPDATED " + eventSource.getCurrentData()))
                .onOpen(() -> System.out.println("And we are... live!"))
                .open();

        Thread.sleep(10000);

        eventSource.close();

        /*
         * Using StreamApiClient
         */
        {
            Subscription subscription = StreamApiClient.createEventStream(apiURL, appKey)
                    .addStreamHeader("X-MYAPI-HEADER", "Polled_By_SD.io")
                    .toObservable()
                    .doOnSubscribe(() -> System.out.println("And we are... back!"))
                    .subscribe(event -> {
                        if (event.isData()) {
                            System.out.println("RX INITIAL DATA " + event.getNode());
                        } else if (event.isPatch()) {
                            System.out.println("RX INITIAL PATCH " + event.getNode());
                        } else if (event.isError()) {
                            throw new RuntimeException(event.getError());
                        }
                    }, Throwable::printStackTrace);


            Thread.sleep(10000);

            subscription.unsubscribe();
        }

        /*
        * Using StreamApiClient that produces a jsonNode with an updated patch
         */
        {
            Subscription subscription = StreamApiClient.createEventStream(apiURL, appKey)
                    .addStreamHeader("X-MYAPI-HEADER", "Polled_By_SD.io")
                    .toObservable()
                    .doOnSubscribe(() -> System.out.println("And we are... back again!"))
                    .scan((event1, event2) -> {
                        // apply patch a create an event with data
                        if (event1.isData()) {
                            return Event.forData(event1.getNode());
                        } else if (event2.isPatch()) {
                            return Event.forData(JsonPatch.apply(event2.getNode(), event1.getNode()));
                        } else throw new RuntimeException(event1.getError());
                    })
                    .map(Event::getNode)
                    .subscribe(data -> System.out.println("RX DATA " + data), Throwable::printStackTrace);


            Thread.sleep(10000);

            subscription.unsubscribe();
        }

    }

}
