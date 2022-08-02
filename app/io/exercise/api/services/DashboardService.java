package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.BaseModel;
import io.exercise.api.models.Dashboard;

import io.exercise.api.models.DashboardContent;
import io.exercise.api.models.User;
import io.exercise.api.models.validators.AuthenticatedUser;
import io.exercise.api.mongo.IMongoDB;

import org.bson.types.ObjectId;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;


public class DashboardService {

    @Inject
    IMongoDB mongoDB;
    @Inject
    HttpExecutionContext ec;


    public CompletableFuture<List<Dashboard>> all(User user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        List<Dashboard> dashboards = mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboards", Dashboard.class)
                                .find()
                                .filter(Filters.or(
                                        Filters.in("readACL", user.getId().toString()),
                                        Filters.in("writeACL", user.getId().toString()),
                                        Filters.and(
                                                Filters.eq("readACL", List.of(new ArrayList())),
                                                Filters.eq("writeACL", List.of(new ArrayList())))
                                ))
                                .into(new ArrayList<>());

                        List<ObjectId> ids = dashboards.stream().map(BaseModel::getId).collect(Collectors.toList());

                        List<DashboardContent> dashboardContents = mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboardContent", DashboardContent.class)
                                .find()
                                .filter(Filters.in("dashboardID", ids))
                                .filter(Filters.or(
                                        Filters.in("readACL", user.getId().toString()),
                                        Filters.in("writeACL", user.getId().toString())
                                ))
                                .into(new ArrayList<>());

                        return dashboards.stream().peek(x -> {
                                    x.setItems(
                                            dashboardContents.stream().filter(y -> y.getDashboardID().equals(x.getId())).collect(Collectors.toList()));
                                }
                        ).collect(Collectors.toList());
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "SOMETHING WRONG AT DASHBOARD SERVICE " + e.getMessage()));
                    }
                }
                , ec.current());
    }


    public CompletableFuture<Dashboard> save(Dashboard dashboard, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                dashboard.getReadACL().add(user.getId().toString());
                dashboard.getWriteACL().add(user.getId().toString());
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


    public CompletableFuture<Dashboard> update(Dashboard dashboard, String id, User user) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Dashboard> dashboards = mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class)
                        .find()
                        .filter(Filters.in("writeACL", user.getId().toString()))
                        .into(new ArrayList<>());

                if (dashboards.isEmpty()) {
                    throw new RequestException(Http.Status.BAD_REQUEST, "User doesn't exists or its not authenticated! ");
                }

                mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class)
                        .replaceOne(Filters.eq("_id", new ObjectId(id)), dashboard);

                return dashboard;
            } catch (RequestException e) {
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "BAD REQUEST. " + e.getMessage()));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        }, ec.current());
    }

    public CompletableFuture<Dashboard> delete(Dashboard dashboard, String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Dashboard> dashboards = mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class)
                        .find()
                        .filter(Filters.in("writeACL", user.getId().toString()))
                        .into(new ArrayList<>());

                if (dashboards.isEmpty()) {
                    throw new RequestException(Http.Status.BAD_REQUEST, "User doesn't exists or its not authenticated! ");
                }

                mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class)
                        .deleteOne(
                                Filters.eq("_id", new ObjectId(id))
                        );

                mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboardContent", DashboardContent.class)
                        .deleteMany(
                                Filters.eq("dashboardID", new ObjectId(id))
                        );

                return dashboard;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        });
    }


}
