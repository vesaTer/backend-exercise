package io.exercise.api.mongo;

import akka.actor.CoordinatedShutdown;
import com.google.inject.Inject;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;

/**
 * Created by Agon on 09/08/2020
 */
public final class InMemoryMongoDB extends MongoDriver {
	private static MongodExecutable mongoEx;

	@Inject
	public InMemoryMongoDB(CoordinatedShutdown coordinatedShutdown, Config config) {
		super(coordinatedShutdown, config);
	}

	@Override
	public MongoDatabase connect() {
		IRuntimeConfig builder = new RuntimeConfigBuilder()
				.defaults(Command.MongoD)
				.processOutput(ProcessOutput.getDefaultInstanceSilent())
				.build();
		MongodStarter starter = MongodStarter.getInstance(builder);
		try {
			mongoEx = starter.prepare(new MongodConfigBuilder()
					.version(Version.Main.PRODUCTION)
					.net(new Net("localhost", 12345, Network.localhostIsIPv6()))
					.build());
			mongoEx.start();
			client = MongoClients.create("mongodb://localhost:27017");
			return client.getDatabase("test");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public void disconnect() {
		closeMongoClient();
		closeMongoProcess();
	}

	private void closeMongoProcess() {
		if (mongoEx == null) {
			return;
		}
		mongoEx.stop();
	}

	private void closeMongoClient() {
		if (client == null) {
			return;
		}
		client.close();
	}
}
