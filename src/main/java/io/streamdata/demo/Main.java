package io.streamdata.demo;

import io.streamdata.jdk.EventSourceClient;

import java.net.URISyntaxException;

public class Main {

    public static void main(String... args) throws URISyntaxException, InterruptedException {

        final String appKey = "ODZjZDQ5MDYtYzZkYS00NTQwLWI0ZDctMGZlYzU2N2JlYmY3";
        final String apiURL = "http://stockmarket.streamdata.io/prices";


        EventSourceClient eventSource = EventSourceClient.createEventSource(apiURL, appKey);
        eventSource
                .addHeader("X-MYAPI-HEADER", "Polled_By_SD.io")
                .onData(data -> System.out.println("INITIAL DATA " + data))
                .onPatch(patch -> System.out.println("PATCH " + patch + " DATA UPDATED " + eventSource.getCurrentData()))
                .onOpen(() -> System.out.println("And we are... live!"))
                .open();

        Thread.sleep(30000);

        eventSource.close();


    }

}
