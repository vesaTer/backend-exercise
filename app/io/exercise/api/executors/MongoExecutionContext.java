package io.exercise.api.executors;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import play.libs.concurrent.CustomExecutionContext;

/**
 *
 */
public class MongoExecutionContext extends CustomExecutionContext {

	@Inject
	public MongoExecutionContext(ActorSystem actorSystem) {
		// uses a custom thread pool defined in application.conf
		super(actorSystem, "mongo-executor");
	}
}