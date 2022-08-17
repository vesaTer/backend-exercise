package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Aggregates;
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

    /**
     * Get all the dashboard contents of dashboard with id stored in the database that the user has access to
     * @param user that is sending the request
     * @return dashboards
     */
    public CompletableFuture<List<DashboardContent>> all(String id, int skip, int limit,User user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        return mongoDB
                                .getMongoDatabase()
                                .getCollection(collection, DashboardContent.class)
                                .aggregate(List.of(
                                        Aggregates.match(UserUtils.allAcl(user)),
                                        Aggregates.match(Filters.eq("dashboardId", new ObjectId(id))),
                                        Aggregates.skip(skip),
                                        Aggregates.limit(limit)

                                ))
                                .into(new ArrayList<>());

                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "SOMETHING WRONG " + e.getMessage()));
                    }
                }
                , ec.current());

    }

    /**
     * Save a dashboard content in the database
     * @param dashboardContent to be saved
     * @param user that is sending the request
     * @return dashboard content
     */

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

    /**
     * Update a dashboard content in the database
     * @param dashboardContent how we want it
     * @param id of the dashboard content to be updated
     * @param user that is sending the request
     * @return dashboardContent updated
     */
    public CompletableFuture<DashboardContent> update(DashboardContent dashboardContent, String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                dashboardContent.setId(null);
                 mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, DashboardContent.class)
                        .findOneAndReplace(Filters.and(
                                Filters.eq("_id", new ObjectId(id)),
                                Filters.or(UserUtils.writeAcl(user),
                                        UserUtils.isPublic(),
                                        UserUtils.roleWriteAcl(user))), dashboardContent
                        );
                 return dashboardContent;
            }  catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        }, ec.current());
    }

    /**
     * Delete dashboard content from database
     * @param id of the dashboard to be deleted
     * @param user that is sending the request
     *
     * @return deleted dashboard
     */
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
