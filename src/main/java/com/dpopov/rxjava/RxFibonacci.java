import io.reactivex.Observable;

public class RxFibonacci {

    public static Observable<Integer> getFibonacciNumbers() {
        return Observable.create( subscriber -> {
            int prev = 0;
            int current = 1;

            subscriber.onNext(prev); // 0
            subscriber.onNext(current); // 1

            while ( !subscriber.isDisposed() ) {
                final int oldPrev = prev;
                prev = current;

                current += oldPrev;

                subscriber.onNext(current);
            }

//            subscriber.onNext();
//            subscriber.onComplete();
//            subscriber.onError();
        } );
    }
}