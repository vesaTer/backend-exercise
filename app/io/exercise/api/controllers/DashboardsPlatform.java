package io.exercise.api.controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.auth0.jwt.JWT;

import com.auth0.jwt.algorithms.Algorithm;

import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import io.exercise.api.actors.ChatActor;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.ChatRoom;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;
import io.exercise.api.models.validators.AuthenticatedUser;


import io.exercise.api.mongo.IMongoDB;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

import static com.mongodb.client.model.Filters.*;


import io.exercise.api.services.DashboardService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;

import io.exercise.api.utils.Hash;
import io.exercise.api.utils.ServiceUtils;

import play.libs.F;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.mvc.*;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class DashboardsPlatform extends Controller {

    @Inject
    IMongoDB mongoDB;

    @Inject
    SerializationService serializationService;

    @Inject
    DashboardService service;

    @Inject
    private ActorSystem actorSystem;
    @Inject
    private Materializer materializer;

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
//                            eq("password", user.getPassword())
                    ))
                    .first();

            if (user1 == null) {
                return notFound();
            }

            if (!Hash.checkPassword(user.getPassword(), user1.getPassword())) {
                return badRequest("Wrong username or password!");
            }

            String secret = config.getString("play.http.secret.key");
//            Algorithm algorithm = Algorithm.HMAC256("secret");
            Algorithm algorithm = Algorithm.HMAC256(secret);
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

    @AuthenticatedUser
    public CompletableFuture<Result> all(Http.Request request) {
        return service.all(ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }


    public CompletableFuture<Result> hierarchy(Http.Request request) {
        return service.hierarchy()
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @AuthenticatedUser
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> save(Http.Request request) {
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose((dashboards) -> service.save(dashboards, ServiceUtils.getUserFrom(request)))
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
        return service.delete(id, ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }


    public WebSocket chat(String room, String token) {
        return WebSocket.Text.acceptOrResult(request ->
        {
            try {
                User user = ServiceUtils
                        .decodeToken(token)
                        .thenCompose((id) -> ServiceUtils.getUserFromId(mongoDB, id))
                        .thenCompose(u -> ServiceUtils.verify(u, token, config))
                        .join();
                if (user == null) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("User not found!")));
                }
                ChatRoom chatRoom = mongoDB
                        .getMongoDatabase()
                        .getCollection("rooms", ChatRoom.class)
                        .find()
                        .filter(Filters.eq("name", room))
//                        .filter(UserUtils.allAcl(user))
                        .first();

                if (chatRoom == null) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Chat not found!")));
                }




                boolean read = false, write = false;
                List<String> roles = user.getRoles();

                if (chatRoom.getWriteACL().contains(user.getId().toString())) {
                    return CompletableFuture.completedFuture(F.Either.Right(ActorFlow.actorRef((out) ->
                            ChatActor.props(out, room, user, true), actorSystem, materializer))
                    );
                }

                for (String r : roles) {
                    if (chatRoom.getReadACL().contains(r) ||
                            chatRoom.getReadACL().contains(user.getId().toString())) {
                        read = true;
                    }
                    if (chatRoom.getWriteACL().contains(r)) {
                        read = true;
                        write = true;
                    }
                    if (!read) {
                        throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, Json.toJson("Ku je nis o boss!!")));
                    }

                }

                final boolean w = write;

                return CompletableFuture.completedFuture(F.Either.Right(ActorFlow.actorRef((out) -> ChatActor.props(out, room, user, w), actorSystem, materializer)));
            } catch (Exception e) {
                return CompletableFuture.completedFuture(F.Either.Left(badRequest()));
            }

        });
    }


}
