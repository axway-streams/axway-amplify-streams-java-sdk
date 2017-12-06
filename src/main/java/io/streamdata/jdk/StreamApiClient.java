package io.streamdata.jdk;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
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
     *
     * @param scheduler the scheduler to use
     * @return an observable that triggers realtime data
     */
    Flowable<Event> toObservable(Scheduler scheduler);

    /**
     * Expose the arrival of data using {@link Schedulers#computation()}
     *
     * @return an observable that triggers realtime data
     * @see Event
     */
    Flowable<Event> toObservable();


    /**
     * <p>Allow to enable or disable incremental cache. <b>By default incremental cache is enabled</b> thus the following happens : a Snapshot is sent back to the user followed by successive patches of this snapshot.</p>
     * <p>If set to false a snapshot will be sent every time, no patch is sent. This means that {@link Event#getPatch()} will return null <b>Use this only for low frequency polling</b></p>
     * <p>Behind the scene it adds the header <code>text/event-stream</code> for patches or <code>application/json</code> for non-incremental cache</p>
     *
     * @param enableIncrementalCache a boolean to allow incremental cache (default : true)
     */
    StreamApiClient incrementalCache(boolean enableIncrementalCache);


}
