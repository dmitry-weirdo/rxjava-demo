package com.dpopov.rxjava;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;

public class RetryWhenDemo {
	public static void main(String[] args) throws InterruptedException {
		final int[] counterHolder = { 0 };

		Observable
			.create(emitter -> {
				while ( !emitter.isDisposed() ) {
					System.out.println("-----------------------------");
					System.out.println("Entering emitter.onSubscribe.");

					counterHolder[0]++;

					final int counterValue = counterHolder[0];
					System.out.println("New counter value: " + counterValue);

					if (counterValue % 2 == 0) {
						System.out.println("Emitter: emitting onError.");
//						emitter.onError( new RuntimeException(String.format("Exception from source observable. Counter = %d", counterValue)) );
						throw new RuntimeException(String.format("Exception from source observable. Counter = %d", counterValue));
					}

					System.out.println("Emitter: emitting onNext.");
					emitter.onNext(String.format("Normal execution. Counter = %d.", counterValue));
				}

				//				System.out.println("subscribing");
//				emitter.onError(new RuntimeException("always fails"));/
			})
			.retryWhen(attempts -> {
				Observable<Throwable> attempts1 = attempts;

				Observable<Integer> integerObservable = attempts1
					.zipWith(
						Observable.range(1, 100), // another source
						(Throwable n, Integer i) -> i // zipper BiFunction
					);

				Observable<Long> longObservable = integerObservable
					.flatMap(i -> {
//						System.out.println("delay retry by " + i + " second(s)");
//						return Observable.timer(i, TimeUnit.SECONDS);

//						System.out.println("Immediate error.");
//						return Observable.error(new RuntimeException("Immediate error exception."));

						System.out.println("Immediate retry.");
						return Observable.just(666L);
					});

				return longObservable;
			})
			.subscribe(
				(x) -> System.out.println("In subscribe.onNext. Value: " + x),

				(x) -> {
					System.out.println("In subscribe.onError");
					x.printStackTrace();
				}
		  	);

//			.blockingForEach(x -> {
//				System.out.println("In blocking for each: " + x);
//			});

		if (true) {
			sleepAndExit();
			return;
		}
/*
		Flowable
			.just("Hello world")
			.subscribe(System.out::println);


		if (true)
			return;
*/


		Observable<Long> interval = Observable.interval(0, 1, TimeUnit.SECONDS);

		final Observable<Object> source = Observable.create(emitter -> {
			while ( !emitter.isDisposed() ) {
				counterHolder[0]++;

				final int counterValue = counterHolder[0];
				if (counterValue % 2 == 0 )
					throw new RuntimeException(String.format("Exception from source observable. Counter = %d", counterValue));

				System.out.printf("Normal execution. Counter = %d\n", counterValue);
			}
		})
			.repeat(5);


//		interval.subscribeOn(Schedulers.io()).subscribe(
/*
		source.subscribe(
			value -> System.out.println("onNext: " + value),
			value -> System.out.println("onError: " + value),
			() -> System.out.println("onComplete (no arguments)"),
			value -> System.out.println("onSubscribe: " + value)
		);
*/


//		if (true)
//			return;

		final Observable<Object> retryWhenObservable = source.retryWhen(throwable -> {
			System.out.println("I am in retryWhen handler");
			final Observable<Throwable> throwable2 = throwable;

			// this handler must return ObservableSource

//			return Observable.just(true);
			return Observable.error(throwable.blockingFirst());
		});

		retryWhenObservable.subscribe(
			value -> System.out.println("onNext: " + value),
			value -> System.out.println("onError: " + value),
			() -> System.out.println("onComplete (no arguments)"),
			value -> System.out.println("onSubscribe: " + value)
		);


		sleepAndExit();
	}

	private static void sleepAndExit() throws InterruptedException {
		Thread.sleep(10000);
		System.out.println("=======================================");
		System.out.println("Timeout ended. Exiting the main thread.");
	}
}