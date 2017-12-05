package io.streamdata.jdk;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.streamdata.jdk.impl.StreamApiClientImpl;

import java.net.URISyntaxException;

public interface StreamApiClient {

    static StreamApiClient createEventStream(String apiUrl, String appKey) throws URISyntaxException {
        return new StreamApiClientImpl(apiUrl, appKey);
    }

    /**
     * Add a header to the polling request (those header will be passed to the request when SD.io will poll the API)
     *
     * @param name  name of the header
     * @param value value of the header
     * @return this client instance for nice fluent api call
     */
    StreamApiClient addHeader(String name, String value);

    /**
     * Expose the arrival of data
     * @param scheduler the scheduler to use (if none provided computation is used by by default TODO @ link)
     * @return an observable that triggers realtime data
     */
    Flowable<Event> toObservable(Scheduler scheduler);


    /**
     * <p>Allow to enable or disable incremental cache. By default incremental cache is enabled thus the following happens : a Snapshot is sent back to the user followed by successive patches.</p>
     * <p>If set to false a snapshot will be sent every time, no patch is sent. <b>Use this only for low frequency polling</b></p>
     *
     * @param enableIncrementalCache a boolean to allow incremental cache (default : true)
     */
    StreamApiClient incrementalCache(boolean enableIncrementalCache);



}
