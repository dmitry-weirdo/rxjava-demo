package com.dpopov.rxjava.v1.operators;

import rx.Observable;
import rx.Subscriber;

public class ToCleanStringOperator implements Observable.Operator<String, String> { // ObservableOperator in rxjava2

    public static ToCleanStringOperator create() {
        return new ToCleanStringOperator();
    }

    private ToCleanStringOperator() { // private constructor
    }


    @Override
    public Subscriber<? super String> call(final Subscriber<? super String> subscriber) {
        return new Subscriber<String>(subscriber) {
            // methods from Observer, Subscriber implements Observer

            @Override
            public void onCompleted() {
                if ( subscriber.isUnsubscribed() )
                    return;

                subscriber.onCompleted();
            }

            @Override
            public void onError(final Throwable e) {
                if ( subscriber.isUnsubscribed() )
                    return;

                subscriber.onError(e);
            }

            @Override
            public void onNext(final String s) {
                if ( subscriber.isUnsubscribed() )
                    return;

                final String result = s.replaceAll("[^A-Za-z0-9]", ""); // remove non-alphanumeric characters
                subscriber.onNext(result);
            }
        };
    }
}