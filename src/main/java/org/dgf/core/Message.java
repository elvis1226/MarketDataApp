package org.dgf.core;

public record Message(String type, String value) {

    public boolean isEmpty() {
        return this.type == null || this.value == null || this.type.isEmpty() || this.value.isEmpty();
    }
}
