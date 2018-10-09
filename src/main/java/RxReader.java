import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RxReader {

    public static Observable<String> lines(final BufferedReader reader) {
        return Observable.<String>create( subscriber -> {
            String line;

            while ( (line = reader.readLine()) != null ) {
                subscriber.onNext(line);

                if ( subscriber.isDisposed() ) {
                    break;
                }
            }

            subscriber.onComplete();
        } )
        .subscribeOn(Schedulers.io()) // run a code on Schedulers.io() thread
        ;
    }

    public static Observable<String> linesFromInput() {
        return lines( new BufferedReader(new InputStreamReader(System.in)) );
    }
}