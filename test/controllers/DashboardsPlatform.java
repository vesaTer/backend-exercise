package controllers;

import akka.actor.CoordinatedShutdown;
import com.fasterxml.jackson.databind.JsonNode;

import com.google.inject.Inject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.Network;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;

import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.mongo.InMemoryMongoDB;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.Hash;
import io.exercise.api.utils.ServiceUtils;
import jnr.ffi.annotations.In;
import org.bson.types.ObjectId;
import org.junit.*;

import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;

import static play.mvc.Http.Status.*;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;

public class DashboardsPlatform extends WithApplication {

    String token;
    private static MongoDatabase db;


    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }


//    @Before
//    public void getToken() {
//        User user = new User(
//                "urim",
//                "password",
//                new ArrayList<>()
//        );
//        final Http.RequestBuilder homeRequest = new Http.RequestBuilder().method("POST")
//                .uri("/api/authenticate/")
//                .bodyJson(Json.toJson(user));
//
//        final Result result = route(app, homeRequest);
//        assertEquals(OK, result.status());
//
//        JsonNode body = Json.parse(contentAsString(result));
//        token = Json.fromJson(body, String.class);
//
//    }


    public static void connect() {
        db = MongoClients.create("mongodb://localhost:27017").getDatabase("test");
    }

    @BeforeClass
    public static void conn() {
        connect();
    }


    @Before
    public void saveUser() {
        User user = new User(
                "vesa",
                "password",
                new ArrayList<>()
        );
        user.getRoles().add("62e90a69e08d17cd567438dd");
        user.setId(new ObjectId("62fb8e0b52939c02c078ddef"));
        final Http.RequestBuilder homeRequest = new Http.RequestBuilder().method("POST")
                .uri("/api/user/")
                .bodyJson(Json.toJson(user));

        final Result result = route(app, homeRequest);
        assertEquals(OK, result.status());

        final Http.RequestBuilder homeRequest1 = new Http.RequestBuilder().method("POST")
                .uri("/api/authenticate/")
                .bodyJson(Json.toJson(user));

        final Result result2 = route(app, homeRequest1);
        assertEquals(OK, result2.status());

        JsonNode body1 = Json.parse(contentAsString(result2));
        token = Json.fromJson(body1, String.class);


        Dashboard dashboard = new Dashboard(
                "test1",
                "testing123456789testing"

        );
        dashboard.getWriteACL().add("62e90a69e08d17cd567438dd");
        dashboard.getWriteACL().add("62e90a69e08d17cd567438dd");


        dashboard.getReadACL().add("62e90a69e08d17cd567438dd");
        dashboard.getReadACL().add("62e90a69e08d17cd567438dd");


        dashboard.setId(new ObjectId("62eb8522f72e7c45a2448eb6"));

        final Http.RequestBuilder homeRequest2 = new Http.RequestBuilder().method("POST")
                .uri("/api/dashboard/")
                .header("token", token)
                .bodyJson(Json.toJson(dashboard));

        final Result result3 = route(app, homeRequest2);
        assertEquals(OK, result3.status());
    }

    @Test
    public void saveUser1() {
        User user = new User(
                "vesa",
                "",
                new ArrayList<>()
        );
        user.getRoles().add("62e90a69e08d17cd567438dd");

        final Http.RequestBuilder homeRequest = new Http.RequestBuilder().method("POST")
                .uri("/api/user/")
                .bodyJson(Json.toJson(user));

        final Result result = route(app, homeRequest);

        assertEquals(BAD_REQUEST, result.status());

    }

    @Test
    public void testSaveDashboard() {
        Dashboard dashboard = new Dashboard(
                "Testing",
                "this should return OK STATUS"

        );
        dashboard.getWriteACL().add("62e90a69e08d17cd567438dd");
        dashboard.getWriteACL().add("62fb8e0b52939c02c078ddef");


        dashboard.getReadACL().add("62e90a69e08d17cd567438dd");
        dashboard.getReadACL().add("62fb8e0b52939c02c078ddef");
        dashboard.setId(new ObjectId("62f9f3e7ab5ca56a6a2c8079"));


        final Http.RequestBuilder homeRequest = new Http.RequestBuilder().method("POST")
                .uri("/api/dashboard/")
                .header("token", token)
                .bodyJson(Json.toJson(dashboard));


        final Result result = route(app, homeRequest);

        assertEquals(OK, result.status());

        JsonNode body = Json.parse(contentAsString(result));
        Dashboard result2 = Json.fromJson(body, Dashboard.class);
        assertEquals(dashboard, result2);
    }

    @Test
    public void testSaveDashboard1() {
        Dashboard dashboard = new Dashboard(
                "T",
                "t"

        );
        dashboard.setId(new ObjectId());

        final Http.RequestBuilder homeRequest = new Http.RequestBuilder().method("POST")
                .uri("/api/dashboard/")
                .header("token", token)
                .bodyJson(Json.toJson(dashboard));

        final Result result = route(app, homeRequest);

        assertEquals(BAD_REQUEST, result.status());
    }


    @Test
    public void testSaveDashboard2() {
        Dashboard dashboard = new Dashboard(
                "Testing",
                "this should return OK STATUS"

        );
        dashboard.getWriteACL().add("62e90a69e08d17cd567438dd");
        dashboard.getWriteACL().add("62fb8e0b52939c02c078ddef");


        dashboard.getReadACL().add("62e90a69e08d17cd567438dd");
        dashboard.getReadACL().add("62fb8e0b52939c02c078ddef");
        dashboard.setId(new ObjectId("62f9f3e7ab5ca56a6a2c8080"));


        final Http.RequestBuilder homeRequest = new Http.RequestBuilder().method("POST")
                .uri("/api/dashboard/")
                .header("token", token)
                .bodyJson(Json.toJson(dashboard));


        final Result result = route(app, homeRequest);

        assertEquals(OK, result.status());

        JsonNode body = Json.parse(contentAsString(result));
        Dashboard result2 = Json.fromJson(body, Dashboard.class);
        assertEquals(dashboard, result2);
    }

    @Test
    public void getUser() {

        final Http.RequestBuilder homeRequest = new Http.RequestBuilder()
                .method("GET")
                .uri("/api/user/");

        final Result result = route(app, homeRequest);
        assertEquals(OK, result.status());

        JsonNode body = Json.parse(contentAsString(result));
        List<User> resultList = DatabaseUtils.parseJsonListOfType(body, User.class);
        assertTrue(resultList.size() != 0);
    }

    @Test
    public void getDashboards() {
        final Http.RequestBuilder homeRequest = new Http.RequestBuilder()
                .method("GET")
                .uri("/api/dashboard/")
                .header("token", token);

        final Result result = route(app, homeRequest);
        assertEquals(OK, result.status());

        JsonNode body = Json.parse(contentAsString(result));
        List<Dashboard> resultList = DatabaseUtils.parseJsonListOfType(body, Dashboard.class);
        assertTrue(resultList.size() != 0);
    }

    @Test
    public void getDashboards1() {

        final Http.RequestBuilder homeRequest = new Http.RequestBuilder()
                .method("GET")
                .uri("/api/dashboard/")
                .header("token", "token");

        final Result result = route(app, homeRequest);
        assertEquals(BAD_REQUEST, result.status());

    }

    @Test
    public void testUpdate() {
        Dashboard dashboard = new Dashboard("UPDATED", "this should return OK STATUS");
        final Http.RequestBuilder homeRequest = new Http.RequestBuilder().method("PUT")
                .uri("/api/dashboard/62f9f3e7ab5ca56a6a2c8079")
                .header("token", token)
                .bodyJson(Json.toJson(dashboard));
        final Result result = route(app, homeRequest);
        assertEquals(OK, result.status());

        JsonNode body = Json.parse(contentAsString(result));
        Dashboard result2 = Json.fromJson(body, Dashboard.class);

        assertEquals(dashboard, result2);
    }

    @Test
    public void testUpdate1() {
        Dashboard dashboard = new Dashboard("UPDATED", "this should return BAD REQUEST");
        final Http.RequestBuilder homeRequest = new Http.RequestBuilder().method("PUT")
                .uri("/api/dashboard/62f9f3e7ab5ca56a6a2c8079")
                .header("token", "token")
                .bodyJson(Json.toJson(dashboard));
        final Result result = route(app, homeRequest);
        assertEquals(BAD_REQUEST, result.status());

    }

    @Test
    public void testUpdate2() {
        Dashboard dashboard = new Dashboard("UPDATED", "this should return NOT FOUND");
        final Http.RequestBuilder homeRequest = new Http.RequestBuilder().method("PUT")
                .uri("/api/dashboard/62f9f3e7ab5ca56a6a2c80")
                .header("token", token)
                .bodyJson(Json.toJson(dashboard));
        final Result result = route(app, homeRequest);
        assertEquals(NOT_FOUND, result.status());

    }

    @Test
    public void testDelete() {
        final Http.RequestBuilder homeRequest = new Http.RequestBuilder()
                .method("DELETE")
                .header("token", token)
                .uri("/api/dashboard/62f9f3e7ab5ca56a6a2c8080")
                .bodyJson(Json.toJson(""));
        final Result result = route(app, homeRequest);
        assertEquals(OK, result.status());

    }

    @AfterClass
    public static void cleanup() {
        db.getCollection("users").drop();
        db.getCollection("dashboards").drop();
    }


}
