package io.exercise.api.controllers;

import com.google.inject.Inject;
import io.exercise.api.models.DashboardContent;

import io.exercise.api.models.validators.AuthenticatedUser;

import io.exercise.api.models.validators.ValidObject;
import io.exercise.api.services.DashboardContentService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.ServiceUtils;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;

@AuthenticatedUser
public class DashboardContentController extends Controller {
    @Inject
    SerializationService serializationService;

    @Inject
    DashboardContentService service;

    public CompletableFuture<Result> all(Http.Request request, String id, int skip, int limit) {
        return service.all(id, skip, limit, ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    @ValidObject(type = DashboardContent.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> save(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, DashboardContent.class)
                .thenCompose((dashboardContents) -> service.save(dashboardContents, ServiceUtils.getUserFrom(request)))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    @ValidObject(type = DashboardContent.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, DashboardContent.class)
                .thenCompose(DashboardContent -> service.update(DashboardContent, id, ServiceUtils.getUserFrom(request)))
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
