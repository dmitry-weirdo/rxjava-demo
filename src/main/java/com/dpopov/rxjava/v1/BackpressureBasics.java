package com.dpopov.rxjava.v1;

import com.dpopov.rxjava.Utils;
import rx.BackpressureOverflow;
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
        final int intStreamStart = 1;

//        final int intStreamEnd = 1_000_000_000;
        final int intStreamEnd = 1_00_000_000;


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

        IntStream.range(intStreamStart, intStreamEnd) // does not honor backpressure, obviously
            .forEach(i -> {
//                log("[IntStream]: emitting " + i);
                source.onNext(i);
            })
        ;

        Thread.sleep(5000);

        Utils.printSeparator();


        // skipping methods - just emit one value at some
        log("Using sample");
        final PublishSubject<Integer> subject = PublishSubject.create();

        subject
//            .throttleFirst(100, TimeUnit.MILLISECONDS)
//            .throttleLast(100, TimeUnit.MILLISECONDS)
            .sample(100, TimeUnit.MILLISECONDS)

            .observeOn( Schedulers.computation() )
            .subscribe( ComputeFunction::compute, Throwable::printStackTrace )
        ;


        IntStream.range(intStreamStart, intStreamEnd) // does not honor backpressure, obviously
            .forEach(i -> {
//                log("[IntStream]: emitting " + i);
                subject.onNext(i);
            })
        ;

        Utils.printSeparator();


        // set up a backpressure buffer
        log("Using onBackpressureBuffer");
        Observable.range(1, 1_000_000)
            .onBackpressureBuffer(
                  10 // buffer capacity
                , () -> { // action on buffer overflow
//                    log("onBackpressureBuffer: buffer overflown");
                }
                , BackpressureOverflow.ON_OVERFLOW_DROP_OLDEST // Strategy on buffer overflow
            )
            .observeOn( Schedulers.computation() )
            .subscribe( ComputeFunction::compute, Throwable::printStackTrace )
        ;

        Thread.sleep(5000);
        Utils.printSeparator();


        log("Using onBackpressureDrop");
        Observable.range(1, 1_000_000)
            .onBackpressureDrop(
                i -> { // action on buffer overflow
//                    log("onBackpressureDrop: dropped value " + i);
                }
            )
            .observeOn( Schedulers.computation() )
            .subscribe( ComputeFunction::compute, Throwable::printStackTrace )
        ;

        Thread.sleep(5000);
        Utils.printSeparator();
    }

    private static void log(final String s) {
        Utils.log(BackpressureBasics.class, s);
    }
}