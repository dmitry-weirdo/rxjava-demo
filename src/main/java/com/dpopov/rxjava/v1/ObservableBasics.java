package com.dpopov.rxjava.v1;

import com.dpopov.rxjava.Holder;
import com.dpopov.rxjava.Utils;
import org.junit.Assert;
import rx.Observable;
import rx.Observer;
import rx.Single;
import rx.Subscription;
import rx.observables.BlockingObservable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.util.concurrent.TimeUnit;

/**
 * @see <a href="https://www.baeldung.com/rx-java">https://www.baeldung.com/rx-java</a>
 */
public class ObservableBasics {
    public static void main(String[] args) throws InterruptedException {
        trySimpleObservables();
//        trySchedulers();
        tryConnectableObservables();
        trySingle();
        trySubjects();
        tryUsing();
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


    private static void tryConnectableObservables() throws InterruptedException {
        Utils.printMethodStart("tryConnectableObservables");

        final StringBuilder sb = new StringBuilder("<start>");

        final Observable<Long> observable = Observable.interval(200, TimeUnit.MILLISECONDS); // gets 0, 1, 2,.. in the given time interval
        final ConnectableObservable<Long> connectableObservable = observable.publish(); // publish gets ConnectableObservable from Observable
        final Observable<Long> coWithDoOnUnsubscribe = connectableObservable.doOnUnsubscribe(() -> log("[ConnectableObservable 1].doOnUnsubscribe"));// must execute when subscribers unsubscribed from this ConnectableObservable

        Thread.sleep(300);

        // emitting events does not start before ConnectableObservable#connect called
        final Subscription subscription = coWithDoOnUnsubscribe.subscribe(sb::append);
        log("sb after subscribe, but before connect: " + sb.toString());
        Assert.assertEquals("<start>", sb.toString());

        connectableObservable.connect();
        Thread.sleep(700);
        log("sb after subscribe, but before connect: " + sb.toString());
        Assert.assertEquals("<start>012", sb.toString());

        subscription.unsubscribe();
        log("Subscriber unsubscribed.");

        Assert.assertTrue( subscription.isUnsubscribed() );

        Utils.printSeparator();

        final ConnectableObservable<Long> connectable2 = Observable.interval(200, TimeUnit.MILLISECONDS).publish();
        final Observable<Long> autoConnect = connectable2.autoConnect(2);// auto-connect after 2 subscribers

        // !!! to execute autoConnect, subscribers must subscribe not to the initial ConnectableObservable, but to Observable gotten from ConnectableObservable#autoConnect
        Thread.sleep(100);
        log("Subscribing subscriber 1...");
        final Subscription subscription1 = autoConnect.subscribe(l -> log("[Subscriber 1]. Got number: " + l));
        log("Subscriber 1 subscribed. AutoConnect not yet executed.");

        Thread.sleep(400);
        log("Subscribing subscriber 2...");
        final Subscription subscription2 = autoConnect.subscribe(l -> log("[Subscriber 2]. Got number: " + l));
        log("Subscriber 2 subscribed. AutoConnect must be executed.");

        Thread.sleep(1000);

//        connectable2.connect();
//        log("Explicit connect called.");
//        Thread.sleep(1000);


        subscription1.unsubscribe();
        log("Subscriber 1 unsubscribed.");
        subscription2.unsubscribe();
        log("Subscriber 2 unsubscribed.");

        Thread.sleep(1000);

        Utils.printSeparator();


        // refCount -> makes ConnectableObservable behave as usual Observable, so no #connect call required
        // @see http://reactivex.io/documentation/operators/refcount.html
        final ConnectableObservable<Long> connectable3 = Observable.interval(200, TimeUnit.MILLISECONDS).publish();
//        final Observable<Long> co3withDoOnUnsubscribe = connectable3.doOnUnsubscribe(() -> log("[ConnectableObservable 3].doOnUnsubscribe"));// RefCount Observable must unsubscribe from ConnectableObservable after all subscribers unsubscribed

        final Observable<Long> refCount = connectable3.refCount();
        final Observable<Long> refCountWithDoOnUnsubscribe = refCount.doOnUnsubscribe(() -> log("[RefCountSubscriber].doOnUnsubscribe"));// RefCount Observable must unsubscribe from ConnectableObservable after all subscribers unsubscribed

        log("RefCount Observable created");
        Thread.sleep(1000);

        log("Subscribing RefCountSubscriber...");
        final Subscription refCountSubscription = refCountWithDoOnUnsubscribe.subscribe(
              l -> log("[RefCountSubscriber].onNext. Value: " + l)
            , throwable -> log("[RefCountSubscriber].onError")
            , () -> log("[RefCountSubscriber].onCompleted")
        );
        log("RefCountSubscriber subscribed.");

        Thread.sleep(2000);

        refCountSubscription.unsubscribe();
        log("RefCountSubscriber unsubscribed.");

        Utils.printSeparator();
    }

    private static void trySingle() {
        Utils.printMethodStart("trySingle");

        final StringBuilder sb = new StringBuilder();

        final Single<String> single = Observable
            .just("Hello")
            .toSingle() // there also exists Single.create
        ;

        final Single<String> stringWithCallbacks = single
            .doOnSuccess(sb::append)
            .doOnError(e -> {
                throw new RuntimeException(e);
            })
        ;

        stringWithCallbacks.subscribe();
        log("sb after subscribe: " + sb.toString());

        Assert.assertEquals("Hello", sb.toString());

        Utils.printSeparator();
    }

    private static void trySubjects() {
        Utils.printMethodStart("trySubjects");

        final Holder<Integer> holder1 = new Holder<>(0);
        final Holder<Integer> holder2 = new Holder<>(0);

        final PublishSubject<Integer> subject = PublishSubject.create();

        // first values will be received by first observer/subscriber only
        subject.subscribe( getFirstObserver(holder1) ); // subscribe is Observable method
        log("Observer 1 subscribed");
        subject.onNext(1); // onNext is Observer method implementation
        subject.onNext(2);
        subject.onNext(3);

        // next values will be received by both observers/subscribers
        subject.subscribe( getSecondObserver(holder2) ); // subscribe is Observable method
        log("Observer 2 subscribed");
        subject.onNext(4);

        log("holder1.value: " + holder1.value);
        log("holder2.value: " + holder2.value);

        Assert.assertEquals((Integer) 10, holder1.value);
        Assert.assertEquals((Integer) 4, holder2.value);

        Utils.printSeparator();
    }

    private static Observer<Integer> getFirstObserver(final Holder<Integer> holder) {
        return new Observer<Integer>() {
            @Override
            public void onCompleted() {
                log("Observer 1 completed");
            }

            @Override
            public void onError(final Throwable e) {
                log("Observer 1.onError");
                e.printStackTrace();
            }

            @Override
            public void onNext(final Integer integer) {
                log("Observer1.doNext: got value " + integer);
                holder.value += integer; // todo: bydlocode of changing the external variable
            }
        };
    }
    private static Observer<Integer> getSecondObserver(final Holder<Integer> holder) {
        return new Observer<Integer>() {
            @Override
            public void onCompleted() {
                log("Observer 2 completed");
            }

            @Override
            public void onError(final Throwable e) {
                log("Observer 2.onError");
                e.printStackTrace();
            }

            @Override
            public void onNext(final Integer integer) {
                log("Observer2.doNext: got value " + integer);
                holder.value += integer; // todo: bydlocode of changing the external variable
            }
        };
    }

    private static void tryUsing() {
        Utils.printMethodStart("tryUsing");

        final StringBuilder sb = new StringBuilder("<start>");

        final Observable<Character> values = Observable.using(
              () -> "MyResource" // Func 0: () -> R, R is String in this case
            , r -> Observable.create(subscriber -> {
                for (final Character c : r.toCharArray()) {
                    subscriber.onNext(c);
                }

                subscriber.onCompleted();
            })
            , r -> log("Disposed the resource: " + r)
        );

        values.subscribe(
            sb::append
        );

        System.out.println("Sb after resource usage: " + sb.toString());
        Assert.assertEquals("<start>MyResource", sb.toString());

        Utils.printSeparator();
    }

    private static void log(final String s) {
        Utils.log(ObservableBasics.class, s);
    }
}