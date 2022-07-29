package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.Hash;
import org.bson.types.ObjectId;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Handler;

public class UserService {

    @Inject
    HttpExecutionContext ec;

    @Inject
    IMongoDB mongoDB;

    /**
     * Get all the users stored in the database
     *
     * @return users in cache
     */

    public CompletableFuture<List<User>> all() {
        return CompletableFuture.supplyAsync(() -> mongoDB
                .getMongoDatabase()
                .getCollection("users", User.class)
                .find()
                .into(new ArrayList<>()), ec.current());
    }

    /**
     * Adds a user to the database, if not already in
     *
     * @param user to be added
     * @return the added user or throws an Internal server error if the user is not added
     */

    public CompletableFuture<User> save(User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                user.setPassword(new Hash().createPassword(user.getPassword()));
                mongoDB
                        .getMongoDatabase()
                        .getCollection("users", User.class)
                        .insertOne(user);
                return user;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }
        }, ec.current());
    }

    /**
     * If a user with ID as id exists in the database, update it
     *
     * @param user-the updated user, id- the id of the user
     * @return updated user
     */
    public CompletableFuture<User> update(User user, String id) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                mongoDB
                        .getMongoDatabase()
                        .getCollection("users", User.class)
                        .replaceOne(Filters.eq("_id", new ObjectId(id)), user);

                return user;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        }, ec.current());
    }

    /**
     * If a user with an id as the param exists in the database, remove it, else throw a not found exception
     *
     * @param id of the user to be removed
     * @return the removed user
     */
    public CompletableFuture<User> delete(User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                mongoDB
                        .getMongoDatabase()
                        .getCollection("users", User.class)
                        .deleteOne(
                                Filters.eq("_id", new ObjectId(id))
                        );

                return user;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        });
    }
}
