package com.dpopov.rxjava.v1;

import com.dpopov.rxjava.Utils;
import org.junit.Assert;
import rx.Observable;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

/**
 * @see <a href="https://www.baeldung.com/rx-java">https://www.baeldung.com/rx-java</a>
 */
public class ObservableBasics {
    public static void main(String[] args) throws InterruptedException {
        trySimpleObservables();
        trySchedulers();
    }

    private static void trySimpleObservables() {
        // BlockingObservable and Observable#toBlocking not present in rxjava2
        final BlockingObservable blockingObservable = Observable.just(1).toBlocking();

        final String[] result = new String[1];
        final Observable<String> observable = Observable.just("Hello");
        observable.subscribe(s -> result[0] = s);
        Assert.assertEquals("Hello", result[0]);
        log("Result after observable.just: " + result[0]);
        Utils.printSeparator();


        // subscribe with onNext, onError, onCompleted
        final String[] letters = { "a", "b", "c", "d", "e", "f", "g", "h" };
        final Observable<String> abcObservable = Observable.from(letters);

        final StringBuilder sb = new StringBuilder();
        abcObservable.subscribe(
              sb::append // onNext, Action1 (similar to java.util.function.Consumer) -> 1 argument
            , Throwable::printStackTrace // onError, Action1 (similar to java.util.function.Consumer) -> 1 argument
            , () -> sb.append("_Completed")// onCompleted, Action0 (similar to java.lang.Runnable) -> no arguments
        );

        log("sb after onNext and onCompleted: " + sb.toString());
        Assert.assertEquals("abcdefgh_Completed", sb.toString());
        Utils.printSeparator();


        // using map function
        sb.delete(0, sb.length());

        Observable.from(letters)
            .map(String::toUpperCase)
            .subscribe(sb::append)
        ;

        log("sb after map uppercase: "+ sb.toString());
        Assert.assertEquals("ABCDEFGH", sb.toString());
        Utils.printSeparator();


        // flat map from operator returning Observable
        sb.delete(0, sb.length());

        Observable.just("book1", "book2")
//            .map(ObservableBasics::getObservable)
            .flatMap(ObservableBasics::getObservable) // flats Observable<String> to String
            .subscribe(s -> sb.append(s).append("||"))
        ;

        log("sb after observable mapping: " + sb.toString());
        Assert.assertEquals("book1||title_book1||book2||title_book2||", sb.toString());
        Utils.printSeparator();


        // scan operator
        sb.delete(0, sb.length());

        final String[] abc = { "a", "b", "c" };
        Observable.from(abc)
//            .scan(new StringBuilder(), (stringBuilder, str) -> stringBuilder.append(str))
            .scan(new StringBuilder(), StringBuilder::append)
            .subscribe(sb::append) // append StringBuilder from scan to sb
        ;

        log("sb after scan: " + sb.toString());
        Assert.assertEquals("aababc", sb.toString());
        Utils.printSeparator();


        // groupBy operator
        final StringBuilder evenSb = new StringBuilder();
        final StringBuilder oddSb = new StringBuilder();

        final Integer[] numbers = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        Observable.from(numbers)
            .groupBy(i -> ((i % 2) == 0) ? "EVEN" : "ODD") // returns Observable<GroupedObservable<String, Integer>>. Key is "EVEN"/"ODD".
//            .subscribe( (final GroupedObservable<String, Integer> groupedObservable) -> {
            .subscribe( groupedObservable -> { // onNext for GroupedObservable<String, Integer>

                groupedObservable.subscribe( (Integer i) -> { // onNext for Integer given by GroupedObservable, divided by GroupedObservable#key
                    final String key = groupedObservable.getKey();

                    log( String.format("Subscribe to GroupedObservable. Key: %s, value: %d", key, i) );

                    switch (key) {

                        case "EVEN":
                            evenSb.append(i);
                            break;

                        case "ODD":
                            oddSb.append(i);
                            break;

                        default:
                            throw new IllegalStateException("Unknown key: " + key);
                    }
                } );

                }
            )
        ;

        log("sb even after groupBy + subscribe: " + evenSb.toString());
        log("sb odd after groupBy + subscribe: " + oddSb.toString());

        Assert.assertEquals("0246810", evenSb.toString());
        Assert.assertEquals("13579", oddSb.toString());

        Utils.printSeparator();


        // filter operator
        sb.delete(0, sb.length());

        Observable.from(numbers)
            .filter( i -> (i % 2 == 1) )
            .subscribe(sb::append)
        ;

        log("sb filtered odd numbers: " + sb.toString());
        Assert.assertEquals("13579", sb.toString());
        Utils.printSeparator();


        // defaultIfEmpty operator
        sb.delete(0, sb.length());

        Observable.<String>empty()
            .defaultIfEmpty("Default empty value")
            .subscribe(sb::append)
        ;

        log("Default after subscribing to empty Observable: " + sb.toString());
        Assert.assertEquals("Default empty value", sb.toString());
        Utils.printSeparator();


        sb.delete(0, sb.length());
        Observable.from(letters)
            .defaultIfEmpty("Default empty value")
            .first() // in rxjava2, first() will explicitly return Single<T>
            .subscribe(sb::append)
        ;

        log("Getting first after non-empty Observable: " + sb.toString());
        Assert.assertEquals("a", sb.toString());
        Utils.printSeparator();


        final int[] sum;

        // takeWhile operator
        sum = new int[]{ 0 };
        Observable.from(numbers)
            .takeWhile( i -> i < 5 )
            .subscribe( i -> sum[0] += i )
        ;

        log("Sum of numbers while less than 5: " + sum[0]);
        Assert.assertEquals(10, sum[0]);
        Utils.printSeparator();


        // takeUntil operator
        sum[0] = 0;
        Observable.from(numbers)
            .takeUntil( i -> i >= 5 ) // !first matching element also included!
            .subscribe( i -> sum[0] += i )
        ;

        log("Sum of numbers until >= 5: " + sum[0]);
        Assert.assertEquals(15, sum[0]);
        Utils.printSeparator();


        // skipWhile
        sum[0] = 0;
        Observable.from(numbers)
            .skipWhile( i -> i <= 5 ) // 6 + 7 + 8 + 9 + 10
            .subscribe( i -> sum[0] += i )
        ;

        log("Sum of numbers skip while <= 5: " + sum[0]);
        Assert.assertEquals(40, sum[0]);
        Utils.printSeparator();


/*
        // skipUntil -> it waits until other Observable emits a value
        sum[0] = 0;
        Observable.from(numbers)
            .skipUntil( getConditionalObservable() ) // 7 + 8 + 9 + 10
            .subscribe( i -> sum[0] += i )
        ;

        log("Sum of numbers skip until > 6: " + sum[0]);
        Assert.assertEquals(34, sum[0]);
        Utils.printSeparator();
*/

        // contains
        final int value1 = 10;
        Observable.from(numbers)
            .contains(value1)
            .subscribe( b -> log(String.format("Number contain %d: %b", value1, b)) )
        ;

        final int value2 = 11;
        Observable.from(numbers)
            .contains(value2)
            .subscribe( b -> log(String.format("Number contain %d: %b", value2, b)) )
        ;

        Utils.printSeparator();
    }
    private static Observable<String> getObservable(final String s) {
        return Observable.just(s, "title_" + s);
    }
/*
    private static Observable<Integer> getConditionalObservable(final Integer i) {
        return Observable.create(subscriber -> {
            if (i > 6) {
                subscriber.onNext(i);
            }

            subscriber.onCompleted();
        } );
    }
*/


    /**
     * @see <a href="http://tomstechnicalblog.blogspot.com/2016/02/rxjava-understanding-observeon-and.html">http://tomstechnicalblog.blogspot.com/2016/02/rxjava-understanding-observeon-and.html</a>
     * @throws InterruptedException in case of {@code Thread.sleep} interrupted
     */
    private static void trySchedulers() throws InterruptedException {
        Utils.printMethodStart("trySchedulers");

        log("Executing default Observable.just, must work on current thread");
        Observable.just(1, 2, 3) // just executes on current thread (no particular scheduler)
            .subscribe( i -> log( String.format("Got number %d on thread \"%s\". \n", i, Thread.currentThread().getName()) ) )
        ;
        Utils.printSeparator();


        log("Executing Observable.create, subscribeOn newThread, observeOn other newThread");
        Observable.<Integer>create(subscriber -> { // todo: use some safe version instead of deprecated. For rxjava2, this signature is ok anyway.
            for (int i = 0; i < 5; i++) {
                log( String.format("[Emitter]: emitting value %d on thread \"%s\"", i, Thread.currentThread().getName()) );
                subscriber.onNext(i);
            }
        })
//            .subscribeOn( Schedulers.computation() )
            .subscribeOn( Schedulers.newThread() )
            .map(i -> { // this is before observeOn, so it works on subscribeOn thread
                log( String.format("[Intermediate subscriber]: Got number %d on thread \"%s\". ", i, Thread.currentThread().getName()) );
                return i * i;
            })
            .observeOn( Schedulers.newThread() ) // this will be different thread from subscribeOn
            .subscribe( i -> log( String.format("[Subscriber]: Got number %d on thread \"%s\". ", i, Thread.currentThread().getName()) ) )
        ;
        Utils.printSeparator();


        Observable.interval(200, TimeUnit.MILLISECONDS) // interval executes on Schedulers.computation(), further "subscribeOn" calls won't change the scheduler
//            .observeOn(Schedulers.computation())
//            .subscribeOn(Schedulers.computation())
            .subscribe( l -> log( String.format("next: %d on thread \"%s\"", l, Thread.currentThread().getName()) ) )
        ;

        Thread.sleep(1000);

        Utils.printSeparator();
    }

    private static void log(final String s) {
        Utils.log(ObservableBasics.class, s);
    }
}