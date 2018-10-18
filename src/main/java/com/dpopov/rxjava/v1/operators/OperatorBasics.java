package com.dpopov.rxjava.v1.operators;

import com.dpopov.rxjava.Utils;
import org.junit.Assert;
import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @see <a href="https://www.baeldung.com/rxjava-custom-operators">https://www.baeldung.com/rxjava-custom-operators</a>
 */
public class OperatorBasics {

    public static void main(String[] args) {
        tryToCleanStringOperator();
        tryToLengthTransformer();
    }

    private static void tryToCleanStringOperator() {
        Utils.printMethodStart("tryToCleanStringOperator");

        final List<String> list = Arrays.asList("John_1", "tom-3", "  -hell- 3 - 4 - abyr");
        final List<String> results = new ArrayList<>();

        final ToCleanStringOperator operator = ToCleanStringOperator.create();

        final Observable<String> observable = Observable
            .from(list)
            .lift(operator) // lift to use Operator
        ;

        observable.subscribe(results::add);

        log("Initial list: " + list);
        log("Results: " + results);

        Assert.assertNotNull(results);
        Assert.assertEquals(3, results.size());
        Assert.assertEquals("John1", results.get(0));
        Assert.assertEquals("tom3", results.get(1));
        Assert.assertEquals("hell34abyr", results.get(2));

        Utils.printSeparator();
    }

    private static void tryToLengthTransformer() {
        Utils.printMethodStart("tryToLengthTransformer");

        final List<String> list = Arrays.asList("", "one", "two", "three");
        final List<Integer> results = new ArrayList<>();

        final ToLengthTransformer transformer = ToLengthTransformer.create();
        final Observable.Transformer<String, Integer> transformer2 = s -> s.map(String::length); // implementation of FunctionalInterface just as lambda

        final Observable<Integer> observable = Observable
            .from(list)
//            .compose(transformer) // compose to use Transformer
            .compose(transformer2) // compose to use Transformer
        ;

        observable.subscribe(results::add, Throwable::printStackTrace);

        log("Initial list: " + list);
        log("Results: " + results);

        Assert.assertNotNull(results);
        Assert.assertEquals(4, results.size());
        Assert.assertEquals((Integer) 0, results.get(0));
        Assert.assertEquals((Integer) 3, results.get(1));
        Assert.assertEquals((Integer) 3, results.get(2));
        Assert.assertEquals((Integer) 5, results.get(3));

        Utils.printSeparator();
    }

    private static void log(final String s) {
        Utils.log(OperatorBasics.class, s);
    }
}