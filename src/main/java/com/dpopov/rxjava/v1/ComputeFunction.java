package com.dpopov.rxjava.v1;

import com.dpopov.rxjava.Holder;
import com.dpopov.rxjava.Utils;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Collection;

public class ComputeFunction {
    private static final int MS_TO_SLEEP = 2;


    public static void compute(final Integer v) {
        try {
            log("Compute integer v: " + v);
            Thread.sleep(MS_TO_SLEEP); // emulate a long-running task
        }
        catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void compute(final Collection<Integer> v) { // for Observable#buffer
        try {
            log("Compute list of integer size: " + v.size());
            log("Compute list of integer: " + v);
            Thread.sleep(MS_TO_SLEEP); // emulate a long-running task
        }
        catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void compute(final Observable<Integer> observable) { // for Observable#window
        log("Compute: got observable " + observable);

        final Holder<Integer> firstValueHolder = new Holder<>(0);
        final Holder<Integer> holder = new Holder<>(0);
        final Holder<Boolean> processedFirstValueHolder = new Holder<>(false);

        observable
            .subscribeOn( Schedulers.computation() )
            .observeOn( Schedulers.computation() )
            .subscribe(
//                  ComputeFunction::compute
                  value -> {
                      if ( !processedFirstValueHolder.value ) {
                          processedFirstValueHolder.value = true;
                          firstValueHolder.value = value;
                          log( String.format("Compute [%s]: Observable %s first value: %d", Utils.formatDate(), observable, value) );
                      }

                      holder.value = value;
                  }

                , Throwable::printStackTrace

                , () -> log( String.format(
                      "Compute [%s]: Observable %s completed. Last value: %d. Diff from first value: %d."
                    , Utils.formatDate()
                    , observable
                    , holder.value
                    , (holder.value - firstValueHolder.value)
                ) )
            )
        ;
    }

    private static void log(final String s) {
        Utils.log(ComputeFunction.class, s);
    }
}