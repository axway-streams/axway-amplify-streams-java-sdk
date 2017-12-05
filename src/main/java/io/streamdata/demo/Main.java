package io.streamdata.demo;

import io.reactivex.disposables.Disposable;
import io.streamdata.jdk.EventSourceClient;
import io.streamdata.jdk.StreamApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

public class Main {

    public static void main(String... args) throws URISyntaxException, InterruptedException {

        final String appKey = "ODZjZDQ5MDYtYzZkYS00NTQwLWI0ZDctMGZlYzU2N2JlYmY3";
        final String apiURL = "http://stockmarket.streamdata.io/prices";

        Logger logger = LoggerFactory.getLogger(Main.class);

        /*
         * Using event source client
         */
        {

            EventSourceClient eventSource = EventSourceClient.createEventSource(apiURL, appKey);
            eventSource
                    .addHeader("X-MYAPI-HEADER", "Polled_By_SD.io")
                    .onSnapshot(data -> logger.info("INITIAL SNAPSHOT {}", data))
                    .onPatch(patch -> logger.info("PATCH {} SNAPSHOT UPDATED {}", patch, eventSource.getCurrentData()))
                    .onOpen(() -> logger.info("And we are... live!"))
                    .open();

            Thread.sleep(10000);

            eventSource.close();
        }

        /*
         * Using StreamApiClient
         */
        {
            StreamApiClient streamApiClient = StreamApiClient.createEventStream(apiURL, appKey);
            Disposable disposable = streamApiClient.addHeader("X-MYAPI-HEADER", "Polled By SD.io")
                    .toObservable(null)
                    .subscribe(event -> {
                        if (event.isSnapshot()) {
                            logger.info("RX INITIAL SNAPSHOT {}", event.getSnapshot());
                        } else if (event.isPatch()) {
                            logger.info("RX PATCH {} SNAPSHOT UPDATED {}", event.getPatch(), event.getSnapshot());
                        } else if (event.isError()) {
                            throw new RuntimeException(event.getError());
                        }
                    }, err -> logger.error(err.getMessage(), err));


            Thread.sleep(15000);

            disposable.dispose();

        }


    }

}
