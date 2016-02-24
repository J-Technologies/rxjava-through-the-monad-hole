package nl.ordina.reactive.streams;

import com.mongodb.*;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.bson.Document;
//import org.junit.Before;
//import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO no f#'ing clue where I found this.
// doesn't compile.
// ?? http://mongodb.github.io/mongo-java-driver-reactivestreams/1.1/getting-started/quick-tour-primer/
// see also http://mongodb.github.io/mongo-java-driver-reactivestreams/
public class MongoConnectionTest {
	private final Logger LOG = LoggerFactory.getLogger(MongoConnectionTest.class);

	private com.mongodb.MongoClient mongo;

	//@Test
	public void testStreaming() {
		// Create a publisher
		final DBCollection collection = mongo.getDB("cv").getCollection("person");
		final Publisher<Document> publisher = null;//collection.find();

		// Non blocking
		//publisher.subscribe(new PrintDocumentSubscriber());

		final Subscriber<Document> subscriber = null;//new PrintDocumentSubscriber();
		publisher.subscribe(subscriber);
		//subscriber.await(); // Block for the publisher to complete
	}

	// TODO this is an IT - depends on running mongodb
	// TODO setup Spring Data Mongo cfg
	//@Test
	public void testMongoConnection() throws UnknownHostException {
		final String groupName = "OSS";
		final DBCollection personCollection = mongo.getDB("cv").getCollection("person");
		LOG.info("count: {}", personCollection.count());
		final DBObject fields = new BasicDBObject("group", groupName);
		final DBObject orderBy = new BasicDBObject("name", -1);
		try (final DBCursor groupCursor = personCollection.find(fields).sort(orderBy)) {
			while (groupCursor.hasNext()) {
				final DBObject personObject = groupCursor.next();
				LOG.info("found {}", personObject);
			}
		}
	}

	//@Before
	public void setUp() throws UnknownHostException {
		final List<ServerAddress> hosts = new ArrayList<>();
		// add more for replica set
		hosts.add(new ServerAddress("127.0.0.1", 27017));
		mongo = new com.mongodb.MongoClient(hosts);
	}

	public void others() {
		// To directly connect to the default server localhost on port 27017
		final MongoClient mongoClient1 = MongoClients.create();

		// Use a Connection String
		final MongoClient mongoClient2 = MongoClients.create("mongodb://localhost");

		// or a Connection String
		final MongoClient mongoClient3 = MongoClients.create(new ConnectionString("mongodb://localhost"));

		// or provide custom MongoClientSettings
		final ClusterSettings clusterSettings = ClusterSettings.builder().hosts(Arrays.asList(new ServerAddress("localhost")))
				.build();
		final MongoClientSettings settings = MongoClientSettings.builder().clusterSettings(clusterSettings).build();
		final MongoClient mongoClient4 = MongoClients.create(settings);
	}
}
