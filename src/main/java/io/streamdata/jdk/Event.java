package io.streamdata.jdk;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This represent an SSE Event processed by stream snapshot. This no generic implementation of an SSE event, for instance it does not contains the id field.
 * It Only handle the three event values sent by a streamdata.io proxy :
 * <ul>
 * <li>snapshot</li>
 * <li>patch</li>
 * <li>error</li>
 * </ul>
 */
public class Event {

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
     * @param snapshot  last know snapshot with patch applied
     * @param patch patch as a json node
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
     * <li>{@link EventSourceClient#incrementalCache(boolean)} or {@link StreamApiClient#incrementalCache(boolean)} has been called with null</li>
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


