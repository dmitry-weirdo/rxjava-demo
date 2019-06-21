package com.dpopov.rxjava;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class StartWithTest {

	public static void main(String[] args) throws InterruptedException {
		System.out.println("==========================");
		System.out.println("Testing with one start value");
		Observable<String> observable1 = createObservable()
			.startWith("I WANNA START WITH THIS");

		subscribe(observable1);

		System.out.println("==========================");
		System.out.println("Testing with list of start values");
		Observable<String> observable2 = createObservable()
			.startWith( Arrays.asList("Start one", "Start two", "Start three") );

		subscribe(observable2);
	}

	private static void subscribe(final Observable<String> observable) throws InterruptedException {
		final Disposable subscription = observable.subscribe(
			value -> System.out.printf("Got value: %s%n", value)
		);

		Thread.sleep(5000);
		subscription.dispose();
		Thread.sleep(1000);
	}

	private static Observable<String> createObservable() {
		final String[] values = { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine" };

		return Observable
			.interval(1, TimeUnit.SECONDS)
			.map(i -> {
				final String value = values[i.intValue() % values.length];
				System.out.printf("I am emitting value: %s%n", value);
				return value;
			});
	}
}