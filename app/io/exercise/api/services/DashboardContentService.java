package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.DashboardContent;
import io.exercise.api.mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class DashboardContentService
{
    @Inject
    IMongoDB mongoDB;
    @Inject
    HttpExecutionContext ec;

    public CompletableFuture<List<DashboardContent>> all() {
        return CompletableFuture.supplyAsync(() -> mongoDB
                .getMongoDatabase()
                .getCollection("dashboards", DashboardContent.class)
                .find()
                .into(new ArrayList<>()), ec.current());
    }


    public CompletableFuture<DashboardContent> save(DashboardContent DashboardContent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboards", DashboardContent.class)
                        .insertOne(DashboardContent);
                return DashboardContent;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }
        }, ec.current());
    }


    public CompletableFuture<DashboardContent> update(DashboardContent DashboardContent, String id) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboards", DashboardContent.class)
                        .replaceOne(Filters.eq("_id", new ObjectId(id)), DashboardContent);

                return DashboardContent;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        }, ec.current());
    }

    public CompletableFuture<DashboardContent> delete(DashboardContent DashboardContent, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboards", DashboardContent.class)
                        .deleteOne(
                                Filters.eq("_id", new ObjectId(id))
                        );

                return DashboardContent;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        });
    }
    
}
