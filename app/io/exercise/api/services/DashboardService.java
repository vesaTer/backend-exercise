package io.exercise.api.services;

import com.google.inject.Inject;


import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;

import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.*;

import io.exercise.api.mongo.IMongoDB;

import io.exercise.api.utils.UserUtils;

import org.bson.BsonNull;
import org.bson.Document;
import org.bson.types.ObjectId;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import java.util.ArrayList;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;


public class DashboardService {

    @Inject
    IMongoDB mongoDB;
    @Inject
    HttpExecutionContext ec;

    private final String collection = "dashboards";

    /**
     * Get all the dashboards stored in the database
     * @param skip how many dashboards to skip
     * @param limit how many dashboards to get
     * @param user that is sending the request
     * @return dashboards
     */
    public CompletableFuture<List<Dashboard>> all(int skip, int limit, User user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        List<Dashboard> dashboards = mongoDB
                                .getMongoDatabase()
                                .getCollection(collection, Dashboard.class)
                                .aggregate(List.of(
                                        Aggregates.match(UserUtils.allAcl(user)),
                                        Aggregates.skip(skip),
                                        Aggregates.limit(limit)
                                        ))
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

    /**
     * Get dashboard hierarchy from database
     * @param skip how many dashboards to skip
     * @param limit how many dashboards to show
     * @param user that is sending the request
     * @return dashboard hierarchy
     */
    public CompletableFuture<List<Dashboard>> hierarchy(int skip, int limit, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Dashboard> dashboards = mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, Dashboard.class)
                        .aggregate(List.of(
//                                Aggregates.match(UserUtils.allAcl(user)),
                                Aggregates.skip(skip),
                                Aggregates.limit(limit),
                                new Document("$match",
                                        new Document("parentId",
                                                new BsonNull())),
                                new Document("$graphLookup",
                                        new Document("from", "dashboards")
                                                .append("startWith", "$_id")
                                                .append("connectFromField", "_id")
                                                .append("connectToField", "parentId")
                                                .append("depthField", "level")
                                                .append("as", "children"))
                        ))
                        .into(new ArrayList<>());


                List<ObjectId> ids = dashboards.stream().map(BaseModel::getId).collect(Collectors.toList());
                dashboards.forEach(dashboard -> ids.addAll(dashboard.getChildren().stream().map(BaseModel::getId).collect(Collectors.toList())));

                List<DashboardContent> dashboardContents = mongoDB
                        .getMongoDatabase()
                        .getCollection("dashboardContent", DashboardContent.class)
                        .find()
                        .filter(Filters.in("dashboardId", ids))
                        .into(new ArrayList<>());

                return dashboards.stream()
                        .peek(dashboard -> {
                            List<Dashboard> children = dashboard.getChildren();
                            List<Dashboard> result1 = children.stream().filter(x -> x.getLevel() == 0).collect(Collectors.toList());
                            result1.forEach(x -> findChildren(x, children));

                            result1.forEach(x -> x.setItems(dashboardContents.stream().filter(y -> Objects.equals(y.getDashboardId(), x.getId())).collect(Collectors.toList())));

                            dashboard.setChildren(result1);
                            dashboard.setItems(dashboardContents.stream().filter(y -> y.getDashboardId().equals(dashboard.getId())).collect(Collectors.toList()));

                        })
                        .collect(Collectors.toList());


            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong. " + e.getMessage()));
            }
        }, ec.current());
    }

    /**
     * Helper recursive method to find children of a dashboard on a list of dashboards
     * @param dashboard to find children to
     * @param dashboards list of all dashboards
     *
     */
    private void findChildren(Dashboard dashboard, List<Dashboard> dashboards) {
        for (Dashboard child : dashboards) {
            if (child.getParentId().equals(dashboard.getId())) {
                dashboard.getChildren().add(child);
                findChildren(child, dashboards);

            }
        }
    }

    /**
     * Save a dashboard in the database
     * @param dashboard to be saved
     * @param user that is sending the request
     * @return dashboard
     */
    public CompletableFuture<Dashboard> save(Dashboard dashboard, User user) {
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
                Dashboard d = mongoDB.getMongoDatabase().getCollection(collection, Dashboard.class).find(Filters.eq("_id",dashboard.getId())).first();
                if(d!=null){
                  return d;
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


    /**
     * Update a dashboard in the database
     * @param dashboard how we want it
     * @param id of the dashboard to be updated
     * @param user that is sending the request
     * @return dashboard updated
     */
    public CompletableFuture<Dashboard> update(Dashboard dashboard, String id, User user) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                 mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, Dashboard.class)
                        .findOneAndReplace(Filters.and(
                                Filters.eq("_id", new ObjectId(id)),
                                Filters.or(UserUtils.writeAcl(user),
                                        UserUtils.isPublic(),
                                        UserUtils.roleWriteAcl(user))), dashboard
                        );
                return dashboard;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        }, ec.current());
    }

    /**
     * Delete dashboard from database
     * @param id of the dashboard to be updated
     * @param user that is sending the request
     *
     * @return deleted dashboard
     */
    public CompletableFuture<Dashboard> delete(String id, User user) {
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
