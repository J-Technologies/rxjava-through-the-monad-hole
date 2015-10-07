/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rx.reactivestreams.example.ratpack;

import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.http.ResponseChunks;
import ratpack.http.client.ReceivedResponse;
import ratpack.sse.ServerSentEvents;
import ratpack.stream.Streams;
import ratpack.test.embed.EmbeddedApp;
import ratpack.test.http.TestHttpClient;
import ratpack.websocket.WebSockets;
import rx.Observable;
import rx.RxReactiveStreams;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

// TODO rewrite
public class RatpackExamples {
	private static final Logger LOG = LoggerFactory.getLogger(RatpackExamples.class);

	private Iterable<Integer> createIntRange(int upper) {
		return ContiguousSet.create(Range.closedOpen(0, upper), DiscreteDomain.integers());
	}

	/**
	 * Tests rendering an observable as a chunked response.
	 * <p/>
	 * Data flow:
	 * <p/>
	 * 1. Observable of ints 2. Convert to Publisher 3. Use Ratpack Publisher
	 * transform 4. Convert to Observable 5. Use Observable transform 6. Convert
	 * to Publisher 7. Render Publisher
	 * <p/>
	 * Back pressure is being applied but it's hard to test for, as it's being
	 * applied by the HTTP client ultimately.
	 */
	@Test
	public void testChunkStreaming() {
		final Iterable<Integer> ints = createIntRange(100);
		EmbeddedApp.fromHandler(ctx -> {
			// Create a publisher
			final Observable<Integer> observable = Observable.from(ints);
			final Publisher<Integer> publisher = RxReactiveStreams.toPublisher(observable);

			// Use one of Ratpack's transforms to convert them into strings
			final Publisher<String> strings = Streams.map(publisher, Object::toString);

			// Convert back to an Rx Observable to do further transforms
			final Observable<String> lines = RxReactiveStreams.toObservable(strings).map(s -> s + "\n");

			// Now render the observable by going back to a publisher
			final Publisher<String> linesPublisher = RxReactiveStreams.toPublisher(lines);

			ctx.render(ResponseChunks.stringChunks(linesPublisher));
		}).test(httpClient -> {
			final String text = httpClient.getText().trim();
			final List<String> strings = Arrays.asList(text.split("\n"));

			final List<Integer> expectedInts = Lists.newArrayList(ints);
			final List<Integer> receivedInts = Lists.transform(strings, Integer::new);

			Assert.assertEquals(expectedInts, receivedInts);
		});
	}

	/**
	 * Test streaming Server Sent Events
	 */
	@Test
	public void testServerSentEvents() {
		final Iterable<Integer> ints = createIntRange(5);

		EmbeddedApp.fromHandler(
				ctx -> {
					final Observable<Integer> observable = Observable.from(ints);
					final Publisher<Integer> publisher = RxReactiveStreams.toPublisher(observable);

					ctx.render(ServerSentEvents.serverSentEvents(publisher,
							e -> e.event("counter").data("event " + e.getItem())));
				}).test(
						httpClient -> {
							final ReceivedResponse response = httpClient.get();
							Assert.assertEquals("text/event-stream;charset=UTF-8", response.getHeaders().get("Content-Type"));

							final String expectedOutput = Arrays.asList(0, 1, 2, 3, 4).stream()
									.map(i -> "event: counter\ndata: event " + i + "\n").collect(joining("\n"))
									+ "\n";

							Assert.assertEquals(expectedOutput, response.getBody().getText());
						});

	}

	/**
	 * Test streaming to a Websocket.
	 *
	 * Note: Ratpack doesn't yet support consuming the incoming data as a
	 * stream.
	 */
	@Test
	public void testWebsocket() {
		final Iterable<Integer> ints = createIntRange(3);

		EmbeddedApp.fromHandler(ctx -> {
			final Observable<String> observable = Observable.from(ints).map(Object::toString);
			WebSockets.websocketBroadcast(ctx, RxReactiveStreams.toPublisher(observable));
		}).test(httpClient -> {
			final URI wsAddress = getWsAddress(httpClient);
			final RecordingWebSocketClient wsClient = new RecordingWebSocketClient(wsAddress);

			try {
				Assert.assertTrue(wsClient.connectBlocking());
				Assert.assertEquals("0", wsClient.next());
				Assert.assertEquals("1", wsClient.next());
				Assert.assertEquals("2", wsClient.next());
				wsClient.waitForClose();
			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private URI getWsAddress(TestHttpClient httpClient) {
		final URI httpAddress = httpClient.getApplicationUnderTest().getAddress();
		URI wsAddress;
		try {
			wsAddress = new URI("ws", null, httpAddress.getHost(), httpAddress.getPort(), httpAddress.getPath(), null,
					null);
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return wsAddress;
	}

}
