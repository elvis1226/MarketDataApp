package org.dgf.core;


public class DataResult<T> {
    public final String error;
    public final T value;

    public DataResult (String error, T value) {
        this.error = error;
        this.value = value;
    }
}
