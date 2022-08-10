package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.DashboardContent;

import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.UserUtils;
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
                                .filter(UserUtils.allAcl(user))
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
                        .getCollection("dashboards", Dashboard.class)
                        .find()
                        .filter(Filters.in("_id", dashboardContent.getDashboardId()))
                        .first();
                if (dashboard == null) {
                    throw new RequestException(Http.Status.NOT_FOUND, "No dashboard matches ");
                }
                if (!dashboardContent.getReadACL().contains(user.getId().toString())) {
                    dashboardContent.getReadACL().add(user.getId().toString());
                }
                if (!dashboardContent.getWriteACL().contains(user.getId().toString())) {
                    dashboardContent.getWriteACL().add(user.getId().toString());
                }

                mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, DashboardContent.class)
                        .insertOne(dashboardContent);
                return dashboardContent;
            } catch (Exception e) {

                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }
        }, ec.current());
    }

    public CompletableFuture<DashboardContent> update(DashboardContent dashboardContent, String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                dashboardContent.setId(null);
                return mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, DashboardContent.class)
                        .findOneAndReplace(Filters.and(
                                Filters.eq("_id", new ObjectId(id)),
                                Filters.or(UserUtils.writeAcl(user),
                                        UserUtils.isPublic(),
                                        UserUtils.roleWriteAcl(user))), dashboardContent
                        );
            }  catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrongg. " + e.getMessage()));
            }

        }, ec.current());
    }

    public CompletableFuture<DashboardContent> delete(String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                return mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, DashboardContent.class)
                        .findOneAndDelete(Filters.and(
                                Filters.eq("_id", new ObjectId(id)),
                                Filters.or(UserUtils.writeAcl(user),
                                        UserUtils.isPublic(),
                                        UserUtils.roleWriteAcl(user)))
                        );

            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        });
    }
}
