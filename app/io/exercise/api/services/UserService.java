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

import java.util.stream.Collectors;

public class UserService {

    @Inject
    HttpExecutionContext ec;

    @Inject
    IMongoDB mongoDB;

    String collection="users";

    /**
     * Get all the users stored in the database
     *
     * @return users
     */

    public CompletableFuture<List<User>> all() {
        return CompletableFuture.supplyAsync(() -> mongoDB
                .getMongoDatabase()
                .getCollection(collection, User.class)
                .find()
                .into(new ArrayList<>()), ec.current());
    }

    /**
     * Adds a user to the database, if not already in
     *
     * @param user to be added
     * @return user
     */

    public CompletableFuture<User> save(User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<String> usernames = mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, User.class)
                        .find()
                        .into(new ArrayList<>())
                        .stream()
                        .map(User::getUsername)
                        .collect(Collectors.toList());

                if (usernames.contains(user.getUsername())){
                    return user;
                }

                user.setPassword(Hash.createPassword(user.getPassword()));
                mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, User.class)
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
                        .getCollection(collection, User.class)
                        .replaceOne(Filters.eq("_id", new ObjectId(id)), user);

                return user;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        }, ec.current());
    }

    /**
     * If a user with an id as the param exists in the database, remove it
     *
     * @param id of the user to be removed
     * @return the removed user
     */
    public CompletableFuture<User> delete(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
             return mongoDB
                        .getMongoDatabase()
                        .getCollection(collection, User.class)
                        .findOneAndDelete(
                                Filters.eq("_id", new ObjectId(id))
                        );

            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Something went wrong. " + e.getMessage()));
            }

        });
    }
}
