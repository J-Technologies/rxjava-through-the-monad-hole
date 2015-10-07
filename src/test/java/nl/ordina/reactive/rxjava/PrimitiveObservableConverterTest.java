package nl.ordina.reactive.rxjava;

import com.google.common.io.ByteStreams;
import org.apache.commons.collections4.IteratorUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import rx.Observable;
import rx.observables.StringObservable;
import rx.observers.TestSubscriber;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static rx.Observable.from;

@RunWith(MockitoJUnitRunner.class)
public class PrimitiveObservableConverterTest {

    @InjectMocks
    PrimitiveObservableConverter cvt;

    @Test
    public void testFromString() throws Exception {
        final Observable<String> stringObservable = cvt.fromString("foo");

        assertEmittedItems(asList("foo"), stringObservable);
    }

    private <T> void assertEmittedItems(List<T> expectedItems, Observable<T> stringObservable) {
        TestSubscriber<T> ts = new TestSubscriber<>();
        stringObservable.subscribe(ts);
        ts.awaitTerminalEvent();
        ts.assertNoErrors();
        assertEquals(expectedItems, ts.getOnNextEvents());
    }

    @Test
    public void testAsString() throws Exception {
        final Observable<String> stringObservable = Observable.just("foo");

        final String stringValue = cvt.asString(stringObservable);

        assertEquals("foo", stringValue);
    }

    @Test
    public void testFromStrings() throws Exception {
        final Observable<String> stringObservable = cvt.fromStrings("foo", "bar", "fnord");

        assertEmittedItems(asList("foo", "bar", "fnord"), stringObservable);
    }

    @Test
    public void testAsStrings() throws Exception {
        final Observable<String> stringObservable = Observable.just("foo", "bar", "fnord");

        final List<String> stringValues = cvt.asStrings(stringObservable);

        assertEquals(asList("foo", "bar", "fnord"), stringValues);
    }

    @Test
    public void testAsStringIterable() throws Exception {
        final Observable<String> stringObservable = Observable.just("foo", "bar", "fnord");

        final Iterable<String> stringValues = cvt.asStringIterable(stringObservable);

        assertEquals(asList("foo", "bar", "fnord"), toList(stringValues));
    }

    private <T> List<T> toList(Iterable<T> values) {
        return IteratorUtils.toList(values.iterator());
    }

    @Test
    public void testFromInt() throws Exception {
        final Observable<Integer> intObservable = cvt.fromInt(42);

        assertEmittedItems(asList(42), intObservable);
    }

    @Test
    public void testFromInts() throws Exception {
        final Observable<Integer> intObservable = cvt.fromInts(42, 314);

        assertEmittedItems(asList(42, 314), intObservable);
    }

    @Test
    public void testFromIntsList() throws Exception {
        final Observable<Integer> intObservable = cvt.fromInts(asList(42, 314));

        assertEmittedItems(asList(42, 314), intObservable);
    }

    @Test
    public void testReadBytes() throws IOException {
        InputStream inputStream = openFile("MSXGames.jpg");

        final Observable<byte[]> bytesObservable = cvt.fromInputStream(inputStream);

        final int expectedItemCount = (int) Math.ceil((double) getFileSize("MSXGames.jpg") / 8192);
        assertEmittedItemCount(expectedItemCount, bytesObservable);
    }

    private <T> void assertEmittedItemCount(int expectedItemCount, Observable<T> bytesObservable) {
        TestSubscriber<T> ts = new TestSubscriber<>();
        bytesObservable.subscribe(ts);
        ts.awaitTerminalEvent();
        ts.assertNoErrors();
        assertEquals(expectedItemCount, ts.getOnNextEvents().size());
    }

    @Test
    public void testReadText() throws IOException {
        InputStream inputStream = openFile("utf8.txt");

        final Observable<String> textObservable = cvt.fromReader(new InputStreamReader(inputStream))
                .doOnNext(text -> System.out.println("value: " + text));

        System.out.println(StringObservable.toString(textObservable));
        assertEmittedItemCount(1, textObservable);
    }

    private long getFileSize(String s) throws IOException {
        return ByteStreams.toByteArray(openFile(s)).length;
    }

    private InputStream openFile(String filename) {
        return this.getClass().getClassLoader().getResourceAsStream(filename);
    }
}