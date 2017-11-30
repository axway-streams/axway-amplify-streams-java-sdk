package io.streamdata.jdk;

import com.fasterxml.jackson.databind.JsonNode;

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

    public Event forData(JsonNode jsonNode) {
        return new Event(EventType.DATA, jsonNode);
    }

    public Event forPatch(JsonNode jsonNode) {
        return new Event(EventType.DATA, jsonNode);
    }

    public Event forError(String error) {
        return new Event(EventType.ERROR, null, error);
    }

    public JsonNode getNode() {
        return node;
    }

    public String getError() {
        return error;
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

    private enum EventType {
        DATA, PATCH, ERROR
    }

}


