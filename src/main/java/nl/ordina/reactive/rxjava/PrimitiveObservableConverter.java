package nl.ordina.reactive.rxjava;

import com.github.davidmoten.rx.Bytes;
import com.github.davidmoten.rx.Checked;
import com.github.davidmoten.rx.Strings;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.apache.http.ObservableHttpResponse;
import rx.observables.StringObservable;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

// TODO split into multiple classes later
// TODO som gebruikte libs op
// TODO ergens anders: rxjava-extras TestingHelper
// TODO from-to:
// https://github.com/davidmoten/rxjava-extras Serialized.read/write
// vert.x
// rxjava-jdbc
// NIO files & sockets
// Iterable
// Future, Callable, Runnable
// JAX-RS client callback
// Set - zie Obs.from(Iterable)
// Map - gebruik entrySet()
// JDK9 Flow
// Java8 streams
// - infinite: generate; kan dat niet eenvoudiger?
// websocket (client+server?)
// guava
// serverkant:
// - JAX-RS @AsyncResponse
// - Spring Web DeferredResult

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

    // TODO maybe refactor these 3 to any type T i.s.o. just String
    Observable<String> fromString(final String message) {
        return Observable.just(message);
    }

    String asString(final Observable<String> message) {
        return message.toBlocking().single();
    }

    Observable<String> fromStrings(final String ... messages) {
        return Observable.from(messages);
    }

    <T> List<T> asList(final Observable<T> observable) {
        return observable.toList().toBlocking().single();
    }

    <T> Iterable<T> asIterable(final Observable<T> observable) {
        return observable.toBlocking().toIterable();
    }

    Observable<Integer> fromInt(final int number) {
        return Observable.just(number);
    }

    Observable<Integer> fromInts(final int ... numbers) {
        return Observable.from(ArrayUtils.toObject(numbers));
    }

    <T> Observable<T> fromList(final List<T> numbers) {
        return Observable.from(numbers);
    }

    <T> Observable<T> fromIteratorPlain(final Iterator<T> iterator) {
        return Observable.from(new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return iterator;
            }
        });
    }

    <T> Observable<T> fromIteratorApacheCommons(Iterator<T> iterator) {
        return Observable.from(IteratorUtils.asIterable(iterator));
    }

    Observable<byte[]> fromInputStream(final InputStream data) {
        // could also use Bytes.from()
        return StringObservable.from(data);
    }

    // TODO side effect warning ahead!! where is the Monad?
    Observable<Integer> toOutputStream(final OutputStream data, Observable<byte[]> src) {
        // TODO rewrite to emit error on write exc
        return src.map(Checked.f1(bytes -> { data.write(bytes); return bytes.length;}));
    }

    Observable<String> fromUtf8InputStream(final InputStream data) {
        return Strings.from(data, Charset.forName("utf8"));
    }

    Observable<String> fromReader(final Reader text) {
        return StringObservable.from(text);
    }

    Observable<String> decodeFromUtf8(Observable<byte[]> encodedText) {
        return StringObservable.decode(encodedText, "UTF8");
    }

    Observable<byte[]> encodeAsUtf8(Observable<String> text) {
        return StringObservable.encode(text, "UTF8");
    }

    // --- these are not really converters.. -- //
    Observable<String> splitByRegex(Observable<String> text, String regex) {
        return StringObservable.split(text, regex);
    }

    Observable<String> splitByLine(Observable<String> text) {
        // could also use Strings.split()
        return StringObservable.byLine(text);
    }

    Observable<String> splitByCharacter(Observable<String> text) {
        return StringObservable.byCharacter(text);
    }

    Observable<String> join(Observable<String> text, CharSequence sep) {
        return StringObservable.join(text, sep);
    }

    Observable<String> concat(Observable<String> text) {
        return StringObservable.stringConcat(text);
    }
    // --- END these are not really converters.. -- //

    <T> Observable<String> toString(Observable<T> src) {
        return StringObservable.toString(src);
    }

    Observable<String> unzipFilterByFilenameAndSplitByLine(InputStream inputstream, String fileToFind) {
        // MUST process the emissions of ZippedEntry synchronously - don't replace concatMap() with flatMap!
        // Reason: the InputStream of each ZippedEntry must be processed fully before moving on to the next one...
        return Bytes.unzip(inputstream)
                        .filter(entry -> entry.getName().equals(fileToFind))
                        .concatMap(entry -> Strings.from(entry.getInputStream()))
                        .compose(o -> Strings.split(o, "\n"));
    }

    Observable<String> asyncGet(String url) {
        // note: CloseableHttpAsyncClient is very expensive to create! It's @ThreadSafe, so it can be used as singleton

        // default
        // CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();

        // custom connection pool & timeouts
        final RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(500).build();
        final CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnPerRoute(20)
                .setMaxConnTotal(50)
                .build();
        httpClient.start();

        /* Note: rxjava-apache-http's ResponseConsumerEventStream does not fully comply with HTML5 SSE; it splits events
         * by a single \r\n. That should be a double \r\n\r\n, to be able to use the 'event:' and 'id:' SSE 'sub-headers'
         * in addition to the standard 'data:' event field.
         */
        Observable<ObservableHttpResponse> responseObservable =
                ObservableHttp
                        .createGet(url, httpClient)
                        // short for: createRequest(HttpAsyncMethods.createGet(url), httpClient)
                        .toObservable();
        // TODO also POST

        return responseObservable.
                doOnError(t -> System.out.println("frack" + t))
        .flatMap(response -> response.getContent().map(bytes -> new String(bytes)));
    }
}
