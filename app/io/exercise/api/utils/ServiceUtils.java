package io.exercise.api.utils;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;

import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import io.exercise.api.actions.Attributes;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;

import io.exercise.api.mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;


import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;



public class ServiceUtils {

    public static String getTokenFromRequest(Http.Request request) {
        Optional<String> optionalToken = request.getHeaders().get("token");
        return optionalToken.orElse(null);
    }

    public static User getUserFrom(Http.Request request) {
        return request.attrs().get(Attributes.USER_TYPED_KEY);
    }

    public static CompletableFuture<String> decodeToken(String token) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        if (token == null) {
                            throw new RequestException(Http.Status.BAD_REQUEST, "Couldn't get token! ");
                        }
                        byte[] decoded = Base64.getDecoder().decode(token.split("\\.")[1]);
                        String decodedString = new String(decoded);
                        JsonNode test = Json.mapper().readTree(decodedString);
                        return test.get("id").asText();
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
                    }


                }
        );
    }

    public static CompletableFuture<User> getUserFromId(IMongoDB mongoDB, String id) {
        return CompletableFuture.supplyAsync(() -> {
            User user = mongoDB
                    .getMongoDatabase()
                    .getCollection("users", User.class)
                    .find(Filters.eq("_id", new ObjectId(id)))
                    .first();
            if (user == null) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("User not found!")));
            }
            return user;
        });
    }

    public static CompletableFuture<User> verify(User user, String token, Config config) {
        return CompletableFuture.supplyAsync(() -> {

            String secret = config.getString("play.http.secret.key");
            Algorithm algorithm = null;
            try {
                algorithm = Algorithm.HMAC256(secret);
            } catch (UnsupportedEncodingException e) {

                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "NOT GOOD" + e.getMessage()));
            }
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            verifier.verify(token);
            return user;
        });
    }

}