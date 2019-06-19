package com.dpopov.rxjava.subscriber_wrapper;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class SubscriberWrapper {

	public static void main(String[] args) throws InterruptedException {
		System.out.println("===========================");
		System.out.println("Testing with PublishSubject");
		final Subject<String> publishSubject = PublishSubject.create();
		testWithSubject(publishSubject);
		System.out.println("Testing with PublishSubject ended");
		System.out.println("=================================");

		System.out.println("===========================");
		System.out.println("Testing with BehaviorSubject");
		final Subject<String> behaviorSubject = BehaviorSubject.create();
		testWithSubject(behaviorSubject);
		System.out.println("Testing with BehaviorSubject ended");
		System.out.println("=================================");

		System.out.println("Waiting more before program end. No emissions should happen...");
		Thread.sleep(5000); // no emissions should happen from here
		System.out.println("Program ended.");
	}

	private static void testWithSubject(final Subject<String> publishSubject) throws InterruptedException {
		final Observable<String> observable = createObservable();

		final SubjectDisposableObserverWrapper<String> wrapper = SubjectDisposableObserverWrapper.create(publishSubject);

		final Disposable disposableForWrapperSubscribedToObservable = observable.subscribeWith(wrapper);

		final Disposable disposableForSubjectObserver = wrapper.subscribe(string -> { // note that we subscribe to wrapper
			System.out.printf("Next value got from wrapper: %s%n", string);
		});

		final Disposable disposableFromInitialObservable = observable.subscribe(string -> { // note that we subscribe to initial observer
			System.out.printf("Next value got from initial observable: %s%n", string);
		});

		Thread.sleep(5000);

		System.out.println("===============================");
		System.out.println("Calling dispose from wrapper...");
		disposableForWrapperSubscribedToObservable.dispose(); // un-subscribe wrapper from its initial Observable. As a result, subscribers to wrapper will stop receiving values
		System.out.println("Dispose from wrapper called.");

		// now only the subscriber to the initial Observable will continue to get emitted values

		Thread.sleep(5000);

		System.out.println("Calling dispose from initial observable...");
		disposableFromInitialObservable.dispose();
		System.out.println("Dispose from initial observable called.");
		System.out.println("=======================================");
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