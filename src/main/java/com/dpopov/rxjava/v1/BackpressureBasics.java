package com.dpopov.rxjava.v1;

import com.dpopov.rxjava.Utils;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @see <a href="https://www.baeldung.com/rxjava-backpressure">https://www.baeldung.com/rxjava-backpressure</a>
 */
public class BackpressureBasics {
    public static void main(String[] args) throws InterruptedException {
//        tryColdObservable();
        tryHotObservable();
    }

    private static void tryColdObservable() throws InterruptedException {
        Utils.printMethodStart("tryColdObservable");

        // Cold Observable, no need for backpressure
        Observable.range(1, 100000) // this operator honors backpressure
            .observeOn( Schedulers.computation() )
            .subscribe(ComputeFunction::compute)
        ;

        Thread.sleep(5000);

        Utils.printSeparator();
    }

    private static void tryHotObservable() throws InterruptedException {
        Utils.printMethodStart("tryHotObservable");

        // Hot Observable that will fail with MissingBackpressureException
        final PublishSubject<Integer> source = PublishSubject.create();

        source
//            .buffer(100) // buffer returns Observable<List<T>>, buffer 100 is not enough
//            .window(500) // window returns Observable<Observable<T>>. Can be useful if subscriber produces batch quicker than one-by-one. // todo: think about this
            .window(1, 500, TimeUnit.MILLISECONDS) // window returns Observable<Observable<T>>. Can be useful if subscriber produces batch quicker than one-by-one. // todo: think about this
//            .buffer(1024) // buffer returns Observable<List<T>>, i.e. subscriber gets list of count values
            .observeOn( Schedulers.computation() )
            .subscribe( ComputeFunction::compute, Throwable::printStackTrace )
        ;

        IntStream.range(1, 1000000000) // does not honor backpressure, obviously
            .forEach(i -> {
//                log("[IntStream]: emitting " + i);
                source.onNext(i);
            })
        ;

        Thread.sleep(5000);

        Utils.printSeparator();
    }

    private static void log(final String s) {
        Utils.log(BackpressureBasics.class, s);
    }
}