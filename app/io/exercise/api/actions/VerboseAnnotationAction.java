package io.exercise.api.actions;

import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

/**
 *
 */
public class VerboseAnnotationAction extends Action<VerboseAnnotation> {

	@Override
    public CompletionStage<Result> call(Http.Request request) {
		if (configuration.value()) {
			Logger.of(this.getClass()).debug("Just logging conditionaly based on my configurated value while being called for {}", request);
		}
		return delegate.call(request);
    }
}
