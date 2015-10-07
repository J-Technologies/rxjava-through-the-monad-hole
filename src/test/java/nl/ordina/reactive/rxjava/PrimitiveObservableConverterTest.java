package nl.ordina.reactive.rxjava;

import com.google.common.io.ByteStreams;
import org.apache.commons.collections4.IteratorUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

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
        TestSubscriber<T> ts = assertSuccessfulTermination(stringObservable);
        assertEquals(expectedItems, ts.getOnNextEvents());
    }

    private void assertEmittedBytes(List<byte[]> expectedItems, Observable<byte[]> bytesObservable) {
        TestSubscriber<byte[]> ts = assertSuccessfulTermination(bytesObservable);

        assertEquals(listToString(expectedItems), listToString(ts.getOnNextEvents()));
    }

    private List<String> listToString(List<byte[]> expectedItems) {
        return cvt.asList(cvt.fromList(expectedItems).map(Arrays::toString));
    }

    private <T> TestSubscriber<T> assertSuccessfulTermination(Observable<T> stringObservable) {
        TestSubscriber<T> ts = new TestSubscriber<>();
        stringObservable.subscribe(ts);
        ts.awaitTerminalEvent();
        ts.assertNoErrors();
        return ts;
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

        final List<String> stringValues = cvt.asList(stringObservable);

        assertEquals(asList("foo", "bar", "fnord"), stringValues);
    }

    @Test
    public void testAsStringIterable() throws Exception {
        final Observable<String> stringObservable = Observable.just("foo", "bar", "fnord");

        final Iterable<String> stringValues = cvt.asIterable(stringObservable);

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
        final Observable<Integer> intObservable = cvt.fromList(asList(42, 314));

        assertEmittedItems(asList(42, 314), intObservable);
    }

    @Test
    public void testFromIterator() throws Exception {
        final Observable<Integer> intObservable = cvt.fromIteratorPlain(asList(42, 314).iterator());
        final Observable<Integer> intObservable2 = cvt.fromIteratorApacheCommons(asList(42, 314).iterator());

        assertEmittedItems(asList(42, 314), intObservable);
        assertEmittedItems(asList(42, 314), intObservable2);
    }

    @Test
    public void testReadBytes() throws IOException {
        InputStream inputStream = openFile("MSXGames.jpg");

        final Observable<byte[]> bytesObservable = cvt.fromInputStream(inputStream);

        final int expectedItemCount = (int) Math.ceil((double) getFileSize("MSXGames.jpg") / 8192);
        assertEmittedItemCount(expectedItemCount, bytesObservable);
    }

    @Test
    public void testWriteBytes() throws IOException {
        Observable<byte[]> bytesObservable = Observable.just("foo".getBytes(), "barf".getBytes());

        ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
        final Observable<Integer> sizeObservable = cvt.toOutputStream(outputstream, bytesObservable);

        assertEmittedItems(asList(3, 4), sizeObservable);
        assertEquals("foobarf", outputstream.toString("utf8"));
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
        final InputStreamReader reader = openUtf8Textfile("utf8.txt");

        final Observable<String> textObservable = cvt.fromReader(reader);

        assertEmittedItems(asList("test 123 € üé\r\nfoo bar fnord\r\n"), textObservable);
    }

    @Test
    public void testReadText2() throws IOException {
        final InputStream stream = openFile("utf8.txt");

        final Observable<String> textObservable = cvt.fromUtf8InputStream(stream);

        assertEmittedItems(asList("test 123 € üé\r\nfoo bar fnord\r\n"), textObservable);
    }

    @Test
    public void testDecode() throws IOException {
        final InputStream data = openFile("utf8.txt");
        final Observable<byte[]> bytesObservable = cvt.fromInputStream(data);

        final Observable<String> textObservable = cvt.decodeFromUtf8(bytesObservable);

        assertEmittedItems(asList("test 123 € üé\r\nfoo bar fnord\r\n"), textObservable);
    }

    @Test
    public void testEncode() throws IOException {
        final InputStreamReader reader = openUtf8Textfile("utf8.txt");
        final Observable<String> textObservable = cvt.fromReader(reader);

        final Observable<byte[]> bytesObservable = cvt.encodeAsUtf8(textObservable);

        assertEmittedBytes(asList("test 123 € üé\r\nfoo bar fnord\r\n".getBytes("utf8")), bytesObservable);
    }

    @Test
    public void testSplitByLine() throws IOException {
        final InputStreamReader reader = openUtf8Textfile("utf8.txt");
        final Observable<String> textObservable = cvt.fromReader(reader);

        final Observable<String> linesObservable = cvt.splitByLine(textObservable);

        assertEmittedItems(asList("test 123 € üé", "foo bar fnord"), linesObservable);
    }

    @Test
    public void testSplitByCharacter() throws IOException {
        final Observable<String> linesObservable = cvt.splitByCharacter(Observable.just("foo"));

        assertEmittedItems(asList("f", "o", "o"), linesObservable);
    }

    @Test
    public void testSplitByRegex() throws IOException {
        final Observable<String> wordObservable = cvt.splitByRegex(Observable.just("foo+bar++fnord"), "\\+");

        assertEmittedItems(asList("foo", "bar", "", "fnord"), wordObservable);
    }

    @Test
    public void testJoin() throws IOException {
        final Observable<String> words = Observable.just("foo", "bar", "", "fnord");

        final Observable<String> wordObservable = cvt.join(words, "+");

        assertEmittedItems(asList("foo+bar++fnord"), wordObservable);
    }

    @Test
    public void testConcat() throws IOException {
        final Observable<String> words = Observable.just("foo", "bar", "", "fnord");

        final Observable<String> wordObservable = cvt.concat(words);

        assertEmittedItems(asList("foobarfnord"), wordObservable);
    }

    @Test
    public void testToString() throws IOException {
        final Observable<Object> objectObservable = Observable.just(new Object() {
            @Override
            public String toString() {
                return "foo";
            }
        });

        final Observable<String> toStringObservable = cvt.toString(objectObservable);

        assertEmittedItems(asList("foo"), toStringObservable);
    }

    @Test
    public void testUnzip() {
        final Observable<String> linesObservable = cvt.unzipFilterByFilenameAndSplitByLine(openFile("foo.zip"), "foo/bar.txt");

        assertEmittedItems(asList("fnord\r","fubar\r","whoops\r","\r", "done\r", ""), linesObservable);
    }

    private InputStreamReader openUtf8Textfile(String filename) throws UnsupportedEncodingException {
        return new InputStreamReader(openFile(filename), "utf8");
    }

    private long getFileSize(String filename) throws IOException {
        return ByteStreams.toByteArray(openFile(filename)).length;
    }

    private InputStream openFile(String filename) {
        return this.getClass().getClassLoader().getResourceAsStream(filename);
    }

}