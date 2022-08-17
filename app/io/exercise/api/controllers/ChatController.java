package io.exercise.api.controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import io.exercise.api.actors.ChatActor;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.ChatRoom;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;
import play.libs.F;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.mvc.Http;
import play.mvc.WebSocket;

import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.forbidden;

public class ChatController {

    @Inject
    IMongoDB mongoDB;
    @Inject
    Config config;

    @Inject
    private ActorSystem actorSystem;
    @Inject
    private Materializer materializer;


    public WebSocket chat(String room, String token) {
        return WebSocket.Json.acceptOrResult(request ->
        {
            try {
                User user = ServiceUtils
                        .decodeToken(token)
                        .thenCompose((id) -> ServiceUtils.getUserFromId(mongoDB, id))
                        .thenCompose(u -> ServiceUtils.verify(u, token, config))
                        .join();
                if (user == null) {
                    throw new RequestException(Http.Status.NOT_FOUND, Json.toJson("User not found!"));
                }
                ChatRoom chatRoom = mongoDB
                        .getMongoDatabase()
                        .getCollection("rooms", ChatRoom.class)
                        .find()
                        .filter(Filters.eq("name", room))
                        .first();

                if (chatRoom == null) {
                    throw new RequestException(Http.Status.NOT_FOUND, Json.toJson("Chat not found!"));
                }
                boolean read = user.canRead(chatRoom);
                if (!read) {
                    throw new RequestException(Http.Status.FORBIDDEN, Json.toJson("You can't enter this room"));
                }

                boolean write = user.canWrite(chatRoom);


                return CompletableFuture.completedFuture(F.Either.Right(ActorFlow.actorRef((out) -> ChatActor.props(out, room, user, write), actorSystem, materializer)));
            } catch (JWTVerificationException e) {
                return CompletableFuture.completedFuture(F.Either.Left(forbidden("User not authenticated!")));
            } catch (RequestException e) {
                return CompletableFuture.completedFuture(F.Either.Left(badRequest(e.getMessage())));
            } catch (Exception e) {
                return CompletableFuture.completedFuture(F.Either.Left(badRequest("Something went wrong!")));
            }

        });
    }
}
