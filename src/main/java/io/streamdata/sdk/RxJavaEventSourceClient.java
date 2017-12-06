package io.streamdata.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public interface RxJavaEventSourceClient {


    /**
     * Add a header to the polling request (those header will be passed to the request when SD.io will poll the API)
     *
     * @param name  name of the header
     * @param value value of the header
     * @return this client instance for nice fluent api call
     */
    RxJavaEventSourceClient addHeader(String name, String value);

    /**
     * Expose the arrival of data
     *
     * @param scheduler the scheduler to use
     * @return an observable that triggers realtime data
     * @see Event
     */
    Flowable<Event> toFlowable(Scheduler scheduler);

    /**
     * Expose the arrival of data using {@link Schedulers#computation()}
     *
     * @return an observable that triggers realtime data
     * @see Event
     */
    Flowable<Event> toFlowable();


    /**
     * <p>Allow to enable or disable incremental cache. <b>By default incremental cache is enabled</b> thus the following happens : a Snapshot is sent back to the user followed by successive patches of this snapshot.</p>
     * <p>If set to false a snapshot will be sent every time, no patch is sent. This means that {@link Event#getPatch()} will return null <b>Use this only for low frequency polling</b></p>
     * <p>Behind the scene it adds the header <code>text/event-stream</code> for patches or <code>application/json</code> for non-incremental cache</p>
     *
     * @param enableIncrementalCache a boolean to allow incremental cache (default : true)
     */
    RxJavaEventSourceClient incrementalCache(boolean enableIncrementalCache);


    /**
     * This represent an SSE Event processed by stream snapshot. This no generic implementation of an SSE event, for instance it does not contains the id field.
     * It Only handle the three event values sent by a streamdata.io proxy :
     * <ul>
     * <li>snapshot</li>
     * <li>patch</li>
     * <li>error</li>
     * </ul>
     */
    class Event {

        private EventType type;
        private JsonNode snapshot;
        private JsonNode patch;
        private String error;

        private Event(EventType type, JsonNode snapshot, JsonNode patch, String error) {
            this.type = type;
            this.snapshot = snapshot;
            this.patch = patch;
            this.error = error;
        }

        private Event(EventType type, JsonNode snapshot, JsonNode patch) {
            this(type, snapshot, patch, null);
        }

        /**
         * Build a event that contains the snapshot.
         *
         * @param snapshot snapshot as a json node
         * @return an Event object
         */
        public static Event forSnapshot(JsonNode snapshot) {
            return new Event(EventType.SNAPSHOT, snapshot, null);
        }

        /**
         * Build a event that contains a patch.
         *
         * @param snapshot last know snapshot with patch applied
         * @param patch    patch as a json node
         * @return an Event object
         */
        public static Event forPatch(JsonNode snapshot, JsonNode patch) {
            return new Event(EventType.PATCH, snapshot, patch);
        }

        /**
         * Build a event that contains an error
         *
         * @param error the json node
         * @return an Event object
         */
        public static Event forError(String error) {
            return new Event(EventType.ERROR, null, null, error);
        }


        /**
         * Gets the snapshot, this method never return a null object. It is either
         * <ul>
         * <li>The initial snapshot {@link #isSnapshot()} returns <code>true</code> is that case</li>
         * <li>The snapshot with the patch applied ({@link #isPatch()} returns <code>true</code> is that case)</li>
         * </ul>
         *
         * @return the snapshot as a JsonNode
         */
        public JsonNode getSnapshot() {
            return snapshot;
        }

        /**
         * Gets the patch if any. There are two cases where <b>the patch can be null</b>
         * <ul>
         * <li>{@link #isSnapshot()} return true</li>
         * <li>{@link EventSourceClient#incrementalCache(boolean)} or {@link RxJavaEventSourceClient#incrementalCache(boolean)} has been called with null</li>
         * </ul>
         *
         * @return the as a JsonNode
         */
        public JsonNode getPatch() {
            return patch;
        }

        public boolean isError() {
            return type == EventType.ERROR;
        }

        public boolean isSnapshot() {
            return type == EventType.SNAPSHOT;
        }

        public boolean isPatch() {
            return type == EventType.PATCH;
        }

        public String getError() {
            return error;
        }

        private enum EventType {
            SNAPSHOT, PATCH, ERROR
        }

    }
}
