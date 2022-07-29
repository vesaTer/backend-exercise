package io.exercise.api.executors;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import play.libs.concurrent.CustomExecutionContext;


public class SingleThreadedExecutionContext extends CustomExecutionContext {

	@Inject
	public SingleThreadedExecutionContext(ActorSystem actorSystem) {
		// uses a custom thread pool defined in application.conf
		super(actorSystem, "single-threaded");
	}
}