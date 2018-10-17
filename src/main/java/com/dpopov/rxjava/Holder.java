package com.dpopov.rxjava;

public class Holder<T> {

    public T value;

    public Holder(final T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
    public void setValue(final T value) {
        this.value = value;
    }
}