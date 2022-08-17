package io.exercise.api.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;

import io.exercise.api.utils.Hash;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static play.mvc.Results.*;

public class Authenticate {

    @Inject
    IMongoDB mongoDB;

    @Inject
    Config config;

    public Result authenticate(Http.Request request) {
        String token;
        try {
            JsonNode node = request.body().asJson();
            User user = Json.fromJson(node, User.class);

            User user1 = mongoDB
                    .getMongoDatabase()
                    .getCollection("users", User.class)
                    .find(and(
                            eq("username", user.getUsername())
                    ))
                    .first();

            if (user1 == null) {
                return notFound();
            }

            if (!Hash.checkPassword(user.getPassword(), user1.getPassword())) {
                return badRequest("Wrong username or password!");
            }

            String secret = config.getString("play.http.secret.key");
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Date dt = new Date();

            token = JWT
                    .create()
                    .withClaim("id", user1.getId().toString())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                    .sign(algorithm);

            return ok(Json.toJson(token));
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest("bad request");
        }

    }
}
