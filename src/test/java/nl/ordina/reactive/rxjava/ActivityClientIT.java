package nl.ordina.reactive.rxjava;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// can be tested via:
// - browser: http://localhost:9999/rest/sse.html
// - cmdline: curl -v http://localhost:9999/rest/webapi/events
//@RunWith(MockitoJUnitRunner.class)
public class ActivityClientIT {
	private static final Logger LOG = LoggerFactory.getLogger(ActivityClientIT.class);

	@InjectMocks
	PrimitiveObservableConverter cvt = new PrimitiveObservableConverter();

	private static final int PORT = 9999;
	private static final String CTX_PATH = "/rest";

	private static Server server;

	@BeforeClass
	public static void startEmbeddedJetty() throws Exception {
		LOG.info("starting embedded Jetty");
		server = new Server(PORT);
		server.setStopAtShutdown(true);

		// Enable parsing of jndi-related parts of web.xml and jetty-env.xml
		final org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList
				.setServerDefault(server);
		classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration",
				"org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");

		final WebAppContext wac = new WebAppContext();
		wac.setContextPath(CTX_PATH);
		wac.setResourceBase("src/main/webapp");
		wac.setClassLoader(ActivityClientIT.class.getClassLoader());
		server.setHandler(wac);
		server.start();
	}

	@Test
	public void shouldGetResponseFromJetty() throws Exception {
		final String userId = "invalid";
		final HttpClient client = HttpClientBuilder.create().build();
		final HttpGet mockRequest = new HttpGet(String.format("http://localhost:%d/%s", PORT, CTX_PATH));
		mockRequest.setHeader("http-user", userId);
		final HttpResponse mockResponse = client.execute(mockRequest);
		final BufferedReader rd = new BufferedReader(new InputStreamReader(mockResponse.getEntity().getContent()));
		assertTrue(rd.readLine().startsWith("<HTML>"));
	}

	@Test
	public void shouldGetTenSseEvents() {
		final Observable<String> eventObservable = cvt.asyncGet("http://localhost:9999/rest/webapi/events");

		// TODO onError/onComplete are 'lost' - is this an error in rxjava-apache-http?
		final List<String> eventData = cvt.asList(eventObservable
//				.doOnNext(s -> System.out.println(s))
//				.doOnError(t -> System.out.println("oeps!" + t)))
				// times four because 4 lines are sent per event (well not really but anyway) - see EventResource
				.take(10 * 4));

		assertEquals(40, eventData.size());
		assertEquals("data: {\"message\":\"msg\",\"id\":9}", eventData.get(39));
	}

	@AfterClass
	public static void shutdownEmbeddedJetty() throws Exception {
		//Thread.sleep(30000);
		server.stop();
	}

}
