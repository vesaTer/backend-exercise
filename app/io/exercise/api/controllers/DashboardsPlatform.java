package io.exercise.api.controllers;


import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;
import io.exercise.api.models.validators.AuthenticatedUser;

import com.google.inject.Inject;

import io.exercise.api.models.validators.ValidObject;
import io.exercise.api.services.DashboardService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;

import io.exercise.api.utils.ServiceUtils;

import play.mvc.*;


import java.util.concurrent.CompletableFuture;

@AuthenticatedUser

public class DashboardsPlatform extends Controller {
    @Inject
    SerializationService serializationService;
    @Inject
    DashboardService service;

    public CompletableFuture<Result> all(Http.Request request, int skip, int limit) {
        return service.all(skip, limit, ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> hierarchy(Http.Request request,int skip, int limit, String id) {
        return service.hierarchy(skip, limit,ServiceUtils.getUserFrom(request), id)
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @ValidObject(type = Dashboard.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> save(Http.Request request) {
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose((dashboards) -> service.save(dashboards, ServiceUtils.getUserFrom(request)))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @ValidObject(type = Dashboard.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose(dashboard -> service.update(dashboard, id, ServiceUtils.getUserFrom(request)))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> delete(Http.Request request, String id) {
        return service.delete(id, ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

}
