package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.DashboardContent;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


public class DashboardContentService {
    @Inject
    IMongoDB mongoDB;
    @Inject
    HttpExecutionContext ec;

   private final String collection = "dashboardContent";

    public CompletableFuture<List<DashboardContent>> all(User user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {

                        return mongoDB
                                .getMongoDatabase()
                                .getCollection(collection, DashboardContent.class)
                                .find()
                                .filter(Filters.or(
                                        Filters.in("readACL", user.getId().toString()),
                                        Filters.in("writeACL",user.getId().toString()),
                                        Filters.and(
                                                Filters.eq("readACL",List.of(new ArrayList<>())),
                                                Filters.eq("writeACL",List.of(new ArrayList<>())))
                                        ))

                                .into(new ArrayList<>());
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "SOMETHING WRONG " + e.getMessage()));
                    }
                }
                , ec.current());

    }


    public CompletableFuture<DashboardContent> save(DashboardContent dashboardContent, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Dashboard dashboard = mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboards",Dashboard.class).find()
                        .filter(Filters.in("_id",dashboardContent.getDashboardID()))
                        .first();
                if (dashboard==null){
                    throw new RequestException(Http.Status.NOT_FOUND,"No dashboard matches ");
                }
                dashboardContent.getReadACL().add(user.getId().toString());
                dashboardContent.getWriteACL().add(user.getId().toString());
                mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, DashboardContent.class)
                        .insertOne(dashboardContent);
                return dashboardContent;
            } catch (Exception e){

                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }
        }, ec.current());
    }


    public CompletableFuture<DashboardContent> update(DashboardContent dashboardContent, String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {


                List<DashboardContent> dashboardContents = mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, DashboardContent.class)
                        .find()
                        .filter(Filters.in("writeACL", user.getId().toString()))
                        .into(new ArrayList<>());
                if (dashboardContents.isEmpty()) {
                    throw new RequestException(Http.Status.BAD_REQUEST, "User doesn't exists or its not authenticated! ");
                }
                System.out.println(dashboardContents);
                dashboardContent.setId(null);
                mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, DashboardContent.class)
                        .replaceOne(Filters.eq("_id", new ObjectId(id)), dashboardContent);

                return dashboardContent;
            } catch (RequestException e) {
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "BAD REQUEST. " + e.getMessage()));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrongg. " + e.getMessage()));
            }

        }, ec.current());
    }

    public CompletableFuture<DashboardContent> delete(DashboardContent dashboardContent, String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                List<DashboardContent> dashboardContents = mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, DashboardContent.class)
                        .find()
                        .filter(Filters.in("writeACL", user.getId().toString()))
                        .into(new ArrayList<>());
                if (dashboardContents.isEmpty()) {
                    throw new RequestException(Http.Status.BAD_REQUEST, "User doesn't exists or its not authenticated! ");
                }
                mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, DashboardContent.class)
                        .deleteOne(
                                Filters.eq("_id", new ObjectId(id))
                        );

                return dashboardContent;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        });
    }
}
