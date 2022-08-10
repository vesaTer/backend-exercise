package io.exercise.api.actions;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;

import com.google.inject.Inject;

import com.typesafe.config.Config;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.models.validators.AuthenticatedUser;

import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.mongodb.client.model.Filters.eq;


public class AuthenticatedUserAction extends Action<AuthenticatedUser> {

    @Inject
    IMongoDB mongoDB;
    @Inject
    Config config;

    @Override
    public CompletionStage<Result> call(Http.Request request) {

        try {
//            String token = ServiceUtils.getTokenFromRequest(request);
//
//            if (token==null){
//                throw new RequestException(Http.Status.BAD_REQUEST, "Couldn't get token! ");
//            }
//
//            String secret = config.getString("play.http.secret.key");
//            Algorithm algorithm = Algorithm.HMAC256(secret);
//            JWTVerifier verifier = JWT.require(algorithm)
//                    .build();
//            DecodedJWT jwt = verifier.verify(token);
//
//            String[] parts = token.split("\\.");
//
//            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
//            JsonNode test = Json.mapper().readTree(payload);
//
//            User user1 = mongoDB
//                    .getMongoDatabase()
//                    .getCollection("users", User.class)
//                    .find(eq("_id", new ObjectId(test.get("id").asText())))
//                    .first();

//
//            if (user1==null){
//                throw new RequestException(Http.Status.BAD_REQUEST, "User doesn't exists! ");
//            }

            String token1 = ServiceUtils.getTokenFromRequest(request);
           User user = ServiceUtils
                    .decodeToken(token1)
                    .thenCompose((id) -> ServiceUtils.getUserFromId(mongoDB, id))
                    .thenCompose(u -> ServiceUtils.verify(u, token1, config))
                    .join();


            request = request.addAttr(Attributes.USER_TYPED_KEY, user);
            return delegate.call(request);

        } catch (JWTVerificationException e) {
            return CompletableFuture.completedFuture(forbidden("TOKEN NOT VERIFIED"));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(badRequest("Something wrong!"));
        }

    }
}
