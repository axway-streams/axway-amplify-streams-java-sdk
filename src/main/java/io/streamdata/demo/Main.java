package io.streamdata.demo;

import io.reactivex.disposables.Disposable;
import io.streamdata.sdk.EventSourceClient;
import io.streamdata.sdk.RxJavaEventSourceClient;
import io.streamdata.sdk.StreamdataClient;
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

            EventSourceClient eventSource = StreamdataClient.createClient(apiURL, appKey);
            eventSource
                    .addHeader("X-MYAPI-HEADER", "Polled-By-SD.io")
                    .addHeader("X-MYAPI-HEADER2", "SomeStuffs")
                    .onSnapshot(data -> logger.info("INITIAL SNAPSHOT {}", data))
                    .onPatch(patch -> logger.info("PATCH {} SNAPSHOT UPDATED {}", patch, eventSource.getCurrentSnapshot()))
                    .onOpen(() -> logger.info("And we are... live!"))
                    .onClose(() -> logger.info("Bye now!"))
                    .open();

            Thread.sleep(10000);

            eventSource.close();
        }

        /*
         * Using event source client (no patch)
         * */
        {

            EventSourceClient eventSource = StreamdataClient.createClient(apiURL, appKey)
                    .incrementalCache(false)
                    .onSnapshot(data -> logger.info("INITIAL SNAPSHOT {}", data))
                    .onOpen(() -> logger.info("And we are... live!"))
                    .open();

            Thread.sleep(10000);

            eventSource.close();
        }

        /*
         * Using RxJavaEventSourceClient
         */
        {
            RxJavaEventSourceClient rxJavaEventSourceClient = StreamdataClient.createRxJavaClient(apiURL, appKey);
            Disposable disposable =
                    rxJavaEventSourceClient.addHeader("X-MYAPI-HEADER", "Polled By SD.io")
                            .incrementalCache(true) // same behavior as default
                            .toFlowable()
                            .subscribe(event -> {
                                if (event.isSnapshot()) {
                                    logger.info("RX INITIAL SNAPSHOT {}", event.getSnapshot());
                                } else if (event.isPatch()) {
                                    logger.info("RX PATCH {} SNAPSHOT UPDATED {}", event.getPatch(), event.getSnapshot());
                                } else if (event.isError()) {
                                    throw new RuntimeException(event.getError().toString());
                                }
                            }, err -> logger.error(err.getMessage(), err));


            Thread.sleep(15000);

            disposable.dispose();

        }


    }

}
