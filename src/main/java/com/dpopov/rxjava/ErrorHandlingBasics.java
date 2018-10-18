package com.dpopov.rxjava;

import io.reactivex.Observable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.observers.TestObserver;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @see <a href="https://www.baeldung.com/rxjava-error-handling">https://www.baeldung.com/rxjava-error-handling</a>
 */
public class ErrorHandlingBasics {

    private static Error UNKNOWN_ERROR = new Error("unknown error");
    private static Exception UNKNOWN_EXCEPTION = new Exception("unknown exception");


    public static void main(String[] args) {
        tryError();
        tryCompositeError();
        tryOnErrorReturn();
        tryOnErrorResumeNext();
        testRetry();
    }

    private static void tryError() {
        Utils.printMethodStart("tryError");

        final TestObserver<Object> testObserver = new TestObserver<>(); // standard test class with helper assert methods, present only in rxjava2 (rxjava1 TestObserver has no assert methods)
        final AtomicBoolean errorOccurred = new AtomicBoolean(false);

        final Observable<Object> observable = Observable
            .error(UNKNOWN_ERROR) // Observable that executes error after first subscriber subscribed
            .doOnError(throwable -> {
                log("Error occurred");
                errorOccurred.set(true);
            })
        ;

        log("Not subscribed. No error yet.");
        Assert.assertFalse( errorOccurred.get() );
        testObserver.assertNoErrors();
        testObserver.assertNotSubscribed();

        log("Subscribing...");
        observable.subscribe(testObserver);
        log("Subscribed");

        log("Error occurred: " + errorOccurred.get());
        Assert.assertTrue( errorOccurred.get() );

        testObserver.assertError(UNKNOWN_ERROR);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();

        Utils.printSeparator();
    }

    private static void tryCompositeError() {
        Utils.printMethodStart("tryCompositeError");

        // throwing error from doError results in CompositeException

        final TestObserver<Object> testObserver = new TestObserver<>(); // standard test class with helper assert methods, rxjava2 only

        Observable
            .error(UNKNOWN_ERROR)
            .doOnError(throwable -> {
                throw new RuntimeException("wrapping RuntimeException"); // throwing an exception in doOnError, this will result in CompositeException
            })
            .subscribe(testObserver)
        ;

        final List<Throwable> errors = testObserver.errors();
        log( String.format("%d errors occurred.", errors.size()) );
/*
        for (final Throwable error : errors) {
            log("---------------------------");
            error.printStackTrace();
            log("---------------------------");
        }
*/

        testObserver.assertError(CompositeException.class);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();

        log("Asserts passed successfully");

        Utils.printSeparator();
    }

    private static void tryOnErrorReturn() {
        Utils.printMethodStart("tryOnErrorReturn");

        final TestObserver<Integer> testObserver = new TestObserver<>();

        final String errorMessage = "Value is even. Throwing exception.";

        final List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        final List<Integer> result = new ArrayList<>();

        Observable
            .fromIterable(list) // from in rxjava 1
            .map(i -> {
                if (i % 2 == 0) {
                    throw new RuntimeException(errorMessage);
                }

                return i;
            })
//            .onErrorReturnItem(-1) // simple form which calls onErrorReturn. Stops emitting original values after this return!
            .onErrorReturn(throwable -> throwable.getMessage().length())
//            .subscribe(testObserver)
            .subscribe(result::add)
        ;

        // if subscribed with result::add
        log("Result after processing: " + result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals((Integer) 1, result.get(0));
//        Assert.assertEquals((Integer) (-1), result.get(1)); // replaced with onErrorReturnItem value
        Assert.assertEquals((Integer) errorMessage.length(), result.get(1)); // replaced with onErrorReturnItem value

/*
        // in case subscribed with testObserver
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValueCount(2);
*/

        Utils.printSeparator();
    }

    private static void tryOnErrorResumeNext() {
        Utils.printMethodStart("tryOnErrorResumeNext");

        final List<String> list = Arrays.asList("one", "two", "three", "four", "five", "six", "seven");
        final List<String> result = new ArrayList<>();

        final String errorMessage = "too long string!";

        Observable
            .fromIterable(list)
            .map(s -> {
               if (s.length() > 4) {
                   throw new RuntimeException(errorMessage);
//                   throw new Error(errorMessage + "!!! Error!!!");
               }

               return s;
            })
            .onErrorResumeNext((Throwable t) -> Observable.just(t.getMessage(), "after throwable"))
//            .onExceptionResumeNext( Observable.just("after exception 1", "after exception 2") ) // onExceptionResumeNext has no access to Throwable! It reacts only to Exceptions, not on Errors
            .subscribe(result::add)
        ;

        log("Result after processing: " + result);

        Assert.assertEquals(4, result.size());
        Assert.assertEquals("one", result.get(0));
        Assert.assertEquals("two", result.get(1));
        Assert.assertEquals(errorMessage, result.get(2));
        Assert.assertEquals("after throwable", result.get(3));

        Utils.printSeparator();
    }

    private static void testRetry() {
        Utils.printMethodStart("testRetry");

        final List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        final List<Integer> result = new ArrayList<>();

        final String errorMessage = "Value is even. Throwing exception.";

        Observable
            .fromIterable(list) // from in rxjava 1
            .map(i -> {
                log("In map. Value: " + i);

                if (i % 2 == 0) {
                    throw new RuntimeException(errorMessage);
                }

                return i;
            })
            .retry( // retry re-subscribes, so that Observable will start from beginning!
                  10 // retry count
                , throwable -> (result.size() < 5) // predicate, if it returns FALSE, then stop retrying
            )
            .subscribe(result::add)
        ;

        log("Result after processing (infinite retry): " + result); // result will be [1 1 1 1 1]

        Utils.printSeparator();
    }


    private static void log(final String s) {
        Utils.log(ErrorHandlingBasics.class, s);
    }
}