package io.exercise.api.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;
import io.exercise.api.models.validators.AuthenticatedUser;
import io.exercise.api.models.validators.ValidDashboard;
import io.exercise.api.models.validators.ValidObject;
import io.exercise.api.mongo.IMongoDB;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

import static com.mongodb.client.model.Filters.*;
import static io.exercise.api.utils.Hash.checkPassword;
import static io.exercise.api.utils.Hash.createPassword;

import io.exercise.api.services.DashboardService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;

import io.exercise.api.utils.ServiceUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.*;


import java.util.Base64;

import java.util.concurrent.CompletableFuture;


public class DashboardsPlatform extends Controller {

    @Inject
    IMongoDB mongoDB;

    @Inject
    SerializationService serializationService;

    @Inject
    DashboardService service;

    public Result authenticate(Http.Request request) {
        String token = "";
        try {
            JsonNode node = request.body().asJson();
            User user = Json.fromJson(node, User.class);


            User user1 = mongoDB
                    .getMongoDatabase()
                    .getCollection("users", User.class)
                    .find(and(
                            eq("username", user.getUsername()),
                            eq("password", user.getPassword())
                    ))
                    .first();


            Algorithm algorithm = Algorithm.HMAC256("secret");
            token = JWT
                    .create()
                    .withClaim("id", user1.getId().toString())
                    .sign(algorithm);

            return ok(Json.toJson(token));
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest("bad request");
        }

    }


    public Result verify(Http.Request request) {

        try {
            String token = request.header("token").get();

            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT jwt = verifier.verify(token);

            String[] parts = token.split("\\.");

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
//            System.out.println(payload);
            JsonNode test = Json.mapper().readTree(payload);

            User user1 = mongoDB
                    .getMongoDatabase()
                    .getCollection("users", User.class)
                    .find(
                            eq("_id", new ObjectId(test.get("id").asText()))
                    )
                    .first();
            return ok(Json.toJson(user1));
        } catch (JWTVerificationException e) {
            return forbidden("TOKEN NOT VERIFIED");
        } catch (Exception e) {
            return internalServerError();
        }

    }


    @AuthenticatedUser
    public CompletableFuture<Result> all(Http.Request request) {
        return service.all(ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @AuthenticatedUser
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> save(Http.Request request) {
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose((dashboards) -> service.save(dashboards,ServiceUtils.getUserFrom(request)))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @AuthenticatedUser
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose(dashboard -> service.update(dashboard, id, ServiceUtils.getUserFrom(request)))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @AuthenticatedUser
    public CompletableFuture<Result> delete(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose(dashboard -> service.delete(dashboard, id, ServiceUtils.getUserFrom(request)))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

}
