package com.dpopov.rxjava.v1.operators;

import rx.Observable;

public class ToLengthTransformer implements Observable.Transformer<String, Integer> { // ObservableTransformer in rxjava2

    public static ToLengthTransformer create() {
        return new ToLengthTransformer();
    }

    private ToLengthTransformer() {
    }

    @Override
    public Observable<Integer> call(final Observable<String> stringObservable) {
        return stringObservable.map(String::length);
    }
}