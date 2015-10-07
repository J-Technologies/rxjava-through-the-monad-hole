package nl.ordina.reactive.rxjava;

import org.apache.commons.lang3.ArrayUtils;
import rx.Observable;
import rx.observables.StringObservable;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

// TODO from-to
// RxJavaString
// vert.x
// RxApacheHttp (apache AsyncHttpClient)
// - single & HTML5 SSE
// File
// scalars (int, ..)
// Iterator
// Iterable
// Future, Callable, Runnable
// JAX-RS client callback
// JAX-RS @AsyncResponse
// List, Set, Map (!)
// JDK9 Flow
// Java8 streams
// - infinite: generate; kan dat niet eenvoudiger?
// websocket
// guava
// Spring Web DeferredResult

// Reactive Streams:
// - ratpack
// - MongoDB
// - Akka Streams

// pi-calculator ScalaDays

// check:
// - rxjava of externe helper modules op github
// - oplossingen van anderen (google, github, reactivex.io, ..

// runnen:
// - unit tests
// - Jetty => integration test?? embedded of via maven??

// uitgangspunten:
// - JEE7 websockets / JAX-RS
// - Java8
public class PrimitiveObservableConverter {

    Observable<String> fromString(final String message) {
        return Observable.just(message);
    }

    String asString(final Observable<String> message) {
        return message.toBlocking().single();
    }

    Observable<String> fromStrings(final String ... messages) {
        return Observable.from(messages);
    }

    List<String> asStrings(final Observable<String> message) {
        return message.toList().toBlocking().single();
    }

    Iterable<String> asStringIterable(final Observable<String> message) {
        return message.toBlocking().toIterable();
    }

    Observable<Integer> fromInt(final int number) {
        return Observable.just(number);
    }

    Observable<Integer> fromInts(final int ... numbers) {
        return Observable.from(ArrayUtils.toObject(numbers));
    }

    Observable<Integer> fromInts(final List<Integer> numbers) {
        return Observable.from(numbers);
    }

    Observable<byte[]> fromInputStream(final InputStream data) {
        return StringObservable.from(data);
    }

    Observable<String> fromReader(final Reader text) {
        return StringObservable.from(text);
    }

    Observable<String> decodeFromUtf8(Observable<byte[]> encodedText) {
        return StringObservable.decode(encodedText, "UTF-8");
    }

    Observable<byte[]> encodeAsUtf8(Observable<String> text) {
        return StringObservable.encode(text, "UTF-8");
    }

    // byLine, byCharacter, toString
}
