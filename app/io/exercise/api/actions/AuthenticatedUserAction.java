package io.exercise.api.actions;

import com.auth0.jwt.exceptions.JWTVerificationException;

import com.google.inject.Inject;

import com.typesafe.config.Config;
import io.exercise.api.models.User;
import io.exercise.api.models.validators.AuthenticatedUser;

import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;

import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class AuthenticatedUserAction extends Action<AuthenticatedUser> {

    @Inject
    IMongoDB mongoDB;
    @Inject
    Config config;

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        try {
            String token1 = ServiceUtils.getTokenFromRequest(request);
            User user = ServiceUtils
                    .decodeToken(token1)
                    .thenCompose((id) -> ServiceUtils.getUserFromId(mongoDB, id))
                    .thenCompose(u -> ServiceUtils.verify(u, token1, config))
                    .join();


            request = request.addAttr(Attributes.USER_TYPED_KEY, user);
            return delegate.call(request);

        } catch (JWTVerificationException e) {
            return CompletableFuture.completedFuture(forbidden("TOKEN NOT VERIFIED"));
        } catch (Exception e) {
//            e.printStackTrace();
            return CompletableFuture.completedFuture(badRequest("Something wrong!" + e.getMessage()));
        }

    }
}
