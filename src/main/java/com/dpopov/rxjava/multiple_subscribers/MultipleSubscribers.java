package com.dpopov.rxjava.multiple_subscribers;

import com.dpopov.rxjava.Utils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;

/**
 * @see <a href="https://www.baeldung.com/rxjava-multiple-subscribers-observable">https://www.baeldung.com/rxjava-multiple-subscribers-observable</a>
 */
public class MultipleSubscribers {

    public static void main(String[] args) throws InterruptedException {
        executeMultipleSubscribersOnObservable();
        executeMultipleSubscribersOnConnectableObservable();
    }

    private static void executeMultipleSubscribersOnObservable() {
        Utils.printMethodStart("executeMultipleSubscribersOnObservable");

        // Observable executes getting values _for each subscriber_
        // doOnDispose is also executed twice
        final Observable<Integer> observable = getObservable();

        log("Subscribing to Observable...");

        // Subscription from rxjava 1 is Disposable in rxjava 2
        final Disposable subscriber1 = observable.subscribe(i -> log("subscriber#1 gets " + i) );
        final Disposable subscriber2 = observable.subscribe( i -> log("subscriber#2 gets " + i) );

        // Subscription.unsubscribe is replaced by Disposable.dispose
        // @see https://blog.kaush.co/2017/06/21/rxjava-1-rxjava-2-disposing-subscriptions/
        subscriber1.dispose();
        log("Subscriber1 disposed.");

        subscriber2.dispose();
        log("Subscriber2 disposed.");

        Utils.printSeparator();
    }

    private static void executeMultipleSubscribersOnConnectableObservable() throws InterruptedException {
        Utils.printMethodStart("executeMultipleSubscribersOnConnectableObservable");

        // ConnectableObservable executes getting values _only once_
        final ConnectableObservable<Integer> connectableObservable = getConnectableObservable();

        log("Subscribing to ConnectableObservable...");

        // Subscription from rxjava 1 is Disposable in rxjava 2
        final Disposable subscriber1 = connectableObservable.subscribe(i -> log("subscriber#1 gets " + i) );
        final Disposable subscriber2 = connectableObservable.subscribe( i -> log("subscriber#2 gets " + i) );

        log("Subscribed to ConnectableObservable...");

        Thread.sleep(1000);

        log("Connecting to ConnectableObservable...");
        final Disposable disposableFromConnect = connectableObservable.connect();// only connect starts emitting the elements


        // Subscription.unsubscribe is replaced by Disposable.dispose
        // @see https://blog.kaush.co/2017/06/21/rxjava-1-rxjava-2-disposing-subscriptions/

        // neither doOnDispose nor doFinally _subscribe_ from called for ConnectableObservable. Not sure whether it is the correct behaviour, @see https://github.com/ReactiveX/RxJava/issues/4624
        subscriber1.dispose();
        log("Subscriber1 disposed.");

        subscriber2.dispose();
        log("Subscriber2 disposed.");

        // dispose on Disposable gotten from ConnectableObservable.connect causes doOnDispose and doFinally to be executed
        disposableFromConnect.dispose();
        log("disposableFromConnect disposed.");

        Utils.printSeparator();
    }


    private static ConnectableObservable<Integer> getConnectableObservable() {
        return getObservable().publish(); // publish returns a ConnectableObservable
    }

    private static Observable<Integer> getObservable() {
        return Observable.<Integer>create( subscriber -> {
            subscriber.onNext( getValue(1) );
            subscriber.onNext( getValue(2) );
        } )
            .doOnDispose( () -> log("Clearing Observable resources on dispose.") ) // doOnDispose replaced doOnUnsubscribe
            .doFinally( () -> log("Clearing Observable resources on finally.") )
        ;
    }

    private static Integer getValue(final int i) {
        log("Getting " + i);
        return i;
    }

    private static void log(final String s) {
        // todo: use logger
        System.out.printf("[%s] %s \n", MultipleSubscribers.class.getSimpleName(), s);
    }
}