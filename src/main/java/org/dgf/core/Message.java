package org.dgf.core;

public class Message {
    private final String type;
    private final String value;

    public Message(String type, String value) {
        this.type = type;
        this.value = value;
    }
    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean isEmpty() {
       return this.type == null || this.value == null || this.type.isEmpty() || this.value.isEmpty();
    }
}
