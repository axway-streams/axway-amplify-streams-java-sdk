package io.streamdata.demo;

import io.streamdata.jdk.EventSourceClient;
import io.streamdata.jdk.StreamApiClient;

import java.net.URISyntaxException;

public class Main {

    public static void main(String... args) throws URISyntaxException, InterruptedException {

        final String appKey = "ODZjZDQ5MDYtYzZkYS00NTQwLWI0ZDctMGZlYzU2N2JlYmY3";
        final String apiURL = "http://stockmarket.streamdata.io/prices";

        /*
         * Using event source client
         */
        {

            EventSourceClient eventSource = EventSourceClient.createEventSource(apiURL, appKey);
            eventSource
                    .addHeader("X-MYAPI-HEADER", "Polled_By_SD.io")
                    .onSnapshot(data -> System.out.println("INITIAL SNAPSHOT " + data))
                    .onPatch(patch -> System.out.println("PATCH " + patch + " SNAPSHOT UPDATED " + eventSource.getCurrentData()))
                    .onOpen(() -> System.out.println("And we are... live!"))
                    .open();

            Thread.sleep(10000);

            eventSource.close();
        }

        /*
         * Using StreamApiClient
         */
        {
            StreamApiClient streamApiClient = StreamApiClient.createEventStream(apiURL, appKey);
            streamApiClient.addStreamHeader("X-MYAPI-HEADER", "Polled_By_SD.io")
                    .toObservable()
                    .doOnSubscribe(() -> System.out.println("And we are... back!"))
                    .subscribe(event -> {

                        if (event.isSnapshot()) {
                            System.out.println("RX INITIAL SNAPSHOT " + event.getSnapshot());
                        } else if (event.isPatch()) {
                            System.out.println("RX PATCH " + event.getPatch() + " SNAPSHOT UPDATED " + event.getSnapshot());
                        } else if (event.isError()) {
                            throw new RuntimeException(event.getError());
                        }
                    }, Throwable::printStackTrace);

            streamApiClient.open();

            Thread.sleep(10000);

            streamApiClient.close();
        }


    }

}
