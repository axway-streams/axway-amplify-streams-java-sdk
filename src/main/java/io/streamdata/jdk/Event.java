package io.streamdata.jdk;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This represent an SSE Event processed by stream data. This no generic implmentation of an SSE event.
 * It Only handle the three event value send by a streamdata proxy :
 * <ul>
 * <li>data</li>
 * <li>patch</li>
 * <li>error</li>
 * </ul>
 */
public class Event {

    private EventType type;
    private JsonNode node;
    private String error;

    private Event(EventType type, JsonNode node, String error) {
        this.type = type;
        this.node = node;
        this.error = error;
    }

    private Event(EventType type, JsonNode node) {
        this(type, node, null);
    }

    /**
     * Build a event that contains the snapshot.
     * @param jsonNode snapshot as a json node
     * @return an Event object
     */
    public static Event forData(JsonNode jsonNode) {
        return new Event(EventType.DATA, jsonNode);
    }

    /**
     * Build a event that contains a patch.
     * @param jsonNode patch as a json node
     * @return an Event object
     */
    public static Event forPatch(JsonNode jsonNode) {
        return new Event(EventType.PATCH, jsonNode);
    }

    /**
     * Build a event that contains an error
     * @param error the json node
     * @return an Event object
     */
    public static Event forError(String error) {
        return new Event(EventType.ERROR, null, error);
    }


    public JsonNode getNode() {
        return node;
    }

    public boolean isError() {
        return type == EventType.ERROR;
    }

    public boolean isData() {
        return type == EventType.PATCH;
    }

    public boolean isPatch() {
        return type == EventType.PATCH;
    }

    public String getError() {
        return error;
    }

    private enum EventType {
        DATA, PATCH, ERROR
    }

}


