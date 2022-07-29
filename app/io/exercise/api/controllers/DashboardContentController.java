package io.exercise.api.controllers;

import com.google.inject.Inject;
import io.exercise.api.models.DashboardContent;

import io.exercise.api.services.DashboardContentService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;

public class DashboardContentController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    DashboardContentService service;

    
    public CompletableFuture<Result> all(Http.Request request, String id) {
        return service.all()
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> save(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, DashboardContent.class)
                .thenCompose((DashboardContents) -> service.save(DashboardContents))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, DashboardContent.class)
                .thenCompose(DashboardContent -> service.update(DashboardContent, id))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }


    
    public CompletableFuture<Result> delete(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, DashboardContent.class)
                .thenCompose(DashboardContent -> service.delete(DashboardContent, id))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
