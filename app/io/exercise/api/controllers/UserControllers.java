package io.exercise.api.controllers;

import com.google.inject.Inject;
import io.exercise.api.models.User;

import io.exercise.api.models.validators.ValidObject;
import io.exercise.api.services.SerializationService;
import io.exercise.api.services.UserService;
import io.exercise.api.utils.DatabaseUtils;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.concurrent.CompletableFuture;


public class UserControllers {

    @Inject
    SerializationService serializationService;

    @Inject
    UserService service;


    public CompletableFuture<Result> all(Http.Request request) {
        return service.all()
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }



    @ValidObject(type = User.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> save(Http.Request request) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose((data) -> service.save(data))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @ValidObject(type = User.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose(user -> service.update(user, id))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> delete(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose(user -> service.delete(user, id))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
