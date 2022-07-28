package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.Dashboard;
import io.exercise.api.mongo.IMongoDB;
import lombok.Data;
import org.bson.types.ObjectId;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class DashboardService {

    @Inject
    IMongoDB mongoDB;
    @Inject
    HttpExecutionContext ec;

    public CompletableFuture<List<Dashboard>> all() {
        return CompletableFuture.supplyAsync(() -> mongoDB
                .getMongoDatabase()
                .getCollection("dashboards", Dashboard.class)
                .find()
                .into(new ArrayList<>()), ec.current());
    }


    public CompletableFuture<Dashboard> save(Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class)
                        .insertOne(dashboard);
                return dashboard;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }
        }, ec.current());
    }


    public CompletableFuture<Dashboard> update(Dashboard dashboard, String id) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class)
                        .replaceOne(Filters.eq("_id", new ObjectId(id)), dashboard);

                return dashboard;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        }, ec.current());
    }

    public CompletableFuture<Dashboard> delete(Dashboard dashboard, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class)
                        .deleteOne(
                                Filters.eq("_id", new ObjectId(id))
                        );

                return dashboard;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        });
    }


}
