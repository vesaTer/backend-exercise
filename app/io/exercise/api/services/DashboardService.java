package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.*;

import io.exercise.api.mongo.IMongoDB;

import io.exercise.api.utils.UserUtils;

import org.bson.Document;
import org.bson.types.ObjectId;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import java.util.ArrayList;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;


public class DashboardService {

    @Inject
    IMongoDB mongoDB;
    @Inject
    HttpExecutionContext ec;

    private final String collection = "dashboards";

    public CompletableFuture<List<Dashboard>> all(User user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        List<Dashboard> dashboards = mongoDB
                                .getMongoDatabase()
                                .getCollection(collection, Dashboard.class)
                                .find()
                                .filter(UserUtils.allAcl(user))
                                .into(new ArrayList<>());

                        List<ObjectId> ids = dashboards.stream().map(BaseModel::getId).collect(Collectors.toList());


                        List<DashboardContent> dashboardContents = mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboardContent", DashboardContent.class)
                                .find()
                                .filter(Filters.in("dashboardId", ids))
                                .filter(UserUtils.allAcl(user))
                                .into(new ArrayList<>());

                        return dashboards.stream().peek(x -> x.setItems(
                                dashboardContents.stream().filter(y -> y.getDashboardId().equals(x.getId())).collect(Collectors.toList()))
                        ).collect(Collectors.toList());
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "SOMETHING WRONG AT DASHBOARD SERVICE " + e.getMessage()));
                    }
                }
                , ec.current());
    }

    public CompletableFuture<List<Dashboard>> hierarchy() {
        return CompletableFuture.supplyAsync(() -> {
            try {

                List<Dashboard> dashboards = mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, Dashboard.class)
                        .aggregate(List.of(new Document("$graphLookup",
                                        new Document("from", "dashboards")
                                                .append("startWith", "$_id")
                                                .append("connectFromField", "_id")
                                                .append("connectToField", "parentId")
                                                .append("depthField", "level")
                                                .append("as", "children"))
                                                ))
                        .into(new ArrayList<>());

                for (Dashboard d: dashboards){
                   d.setChildren(d.getChildren().stream().filter(x->x.getLevel()==0).collect(Collectors.toList()));
                }

                for (Dashboard dashboard : dashboards) {
                    for (Dashboard x: dashboard.getChildren()){
                       x.setChildren(dashboards.stream().filter(y->y.getId().equals(x.getId())).collect(Collectors.toList()));

                    }
                }
                return dashboards.stream().filter(x->x.getParentId()==null).collect(Collectors.toList());
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }
        }, ec.current());
    }

        public CompletableFuture<Dashboard> save (Dashboard dashboard, User user){
            return CompletableFuture.supplyAsync(() -> {
                try {
                    if (!dashboard.getReadACL().contains(user.getId().toString())) {
                        dashboard.getReadACL().add(user.getId().toString());
                    }
                    if (!dashboard.getWriteACL().contains(user.getId().toString())) {
                        dashboard.getWriteACL().add(user.getId().toString());
                    }
                    Dashboard dashboard1 = mongoDB
                            .getMongoDatabase()
                            .getCollection(collection, Dashboard.class)
                            .find()
                            .filter(Filters.in("_id", dashboard.getParentId()))
                            .first();
                    if (dashboard1 == null) {
                        dashboard.setParentId(null);
                    }
                    mongoDB
                            .getMongoDatabase()
                            .getCollection(collection, Dashboard.class)
                            .insertOne(dashboard);
                    return dashboard;
                } catch (Exception e) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
                }
            }, ec.current());
        }


        public CompletableFuture<Dashboard> update (Dashboard dashboard, String id, User user){

            return CompletableFuture.supplyAsync(() -> {
                try {
                    return mongoDB
                            .getMongoDatabase()
                            .getCollection(collection, Dashboard.class)
                            .findOneAndReplace(Filters.and(
                                    Filters.eq("_id", new ObjectId(id)),
                                    Filters.or(UserUtils.writeAcl(user),
                                            UserUtils.isPublic(),
                                            UserUtils.roleWriteAcl(user))), dashboard
                            );

                } catch (Exception e) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
                }

            }, ec.current());
        }

        public CompletableFuture<Dashboard> delete (String id, User user){
            return CompletableFuture.supplyAsync(() -> {
                try {

                    Dashboard dashboard = mongoDB
                            .getMongoDatabase()
                            .getCollection(collection, Dashboard.class)
                            .findOneAndDelete(Filters.and(
                                    Filters.eq("_id", new ObjectId(id)),
                                    Filters.or(UserUtils.writeAcl(user),
                                            UserUtils.isPublic(),
                                            UserUtils.roleWriteAcl(user)))
                            );
                    if (dashboard != null) {
                        mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboardContent", DashboardContent.class)
                                .deleteMany(
                                        Filters.eq("dashboardId", new ObjectId(id))
                                );
                    }
                    return dashboard;

                } catch (Exception e) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
                }

            });
        }


    }
