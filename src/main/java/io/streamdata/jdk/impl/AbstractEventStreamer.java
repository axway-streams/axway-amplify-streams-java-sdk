package io.streamdata.jdk.impl;

import com.google.common.base.Preconditions;
import io.streamdata.jdk.ApiStreamer;

import java.net.URI;
import java.net.URISyntaxException;

abstract class AbstractEventStreamer<T> implements ApiStreamer<T> {

    final StringBuffer url;

    /**
     * Build the url to be called eventually
     *
     * @param apiUrl the api URL
     * @param appKey the key
     * @throws URISyntaxException when the polling URL is not OK
     */
    AbstractEventStreamer(String apiUrl, String appKey) throws URISyntaxException {
        Preconditions.checkNotNull(apiUrl, "apiUrl cannot be null");
        Preconditions.checkNotNull(appKey, "appKey cannot be null");

        // check the url
        URI uri = new URI(apiUrl);

        String queryParamSeparator = (uri.getQuery() == null || uri.getQuery().isEmpty()) ? "?" : "&";

        this.url = new StringBuffer(POLLER_URL)
                .append(apiUrl)
                .append(queryParamSeparator)
                .append("X-Sd-Token=")
                .append(appKey);
    }

    /**
     * Add a header to the polling request
     *
     * @param name  name of the header
     * @param value value of the header
     * @return the object for fluent calls
     */
    public T addHeader(String name, String value) {
        this.url.append('&')
                .append("X-Sd-Header=").append(name).append(':').append(value);
        return (T) this;
    }

}
