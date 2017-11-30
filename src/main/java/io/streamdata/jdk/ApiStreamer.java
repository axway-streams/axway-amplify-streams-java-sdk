package io.streamdata.jdk;

public interface ApiStreamer<T> {

    String POLLER_URL = "https://streamdata.motwin.net/";


    /**
     * Add a header to the polling request (those header will be passed to the request when SD.io will poll the API)
     *
     * @param name  name of the header
     * @param value value of the header
     * @return this client instance for nice fluent api call
     */
    T addHeader(String name, String value);

}
