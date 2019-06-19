package com.dpopov.rxjava.subscriber_wrapper;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.Subject;

public class SubjectDisposableObserverWrapper <T> extends DisposableObserver<T> {
	private final Subject<T> subject;

	private SubjectDisposableObserverWrapper(final Subject<T> subject) {
		this.subject = subject;
	}

	public static <T> SubjectDisposableObserverWrapper<T> create(final Subject<T> subject) {
		return new SubjectDisposableObserverWrapper<>(subject);
	}

	@Override
	public void onNext(final T t) {
		subject.onNext(t);
	}

	@Override
	public void onError(final Throwable e) {
		subject.onError(e);
	}

	@Override
	public void onComplete() {
		subject.onComplete();
	}

	// DisposableObserver#onStart not overridden, it does not get Disposable which is required to call subject#onSubscribe
	public Disposable subscribe(Consumer<? super T> onNext) {
		return subject.subscribe(onNext);
	}
}