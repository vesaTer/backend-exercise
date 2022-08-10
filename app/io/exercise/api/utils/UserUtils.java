package io.exercise.api.utils;

import com.mongodb.client.model.Filters;
import io.exercise.api.models.*;
import org.bson.conversions.Bson;

import java.util.ArrayList;

public class UserUtils {

    public static Bson writeAcl(User user) {
        return Filters.or(
                Filters.in("writeACL", user.getId().toString()));
    }

    public static Bson readAcl(User user) {

        return Filters.or(
                Filters.in("readACL", user.getId().toString()));
    }

    public static Bson roleReadAcl(User user) {
        return Filters.in("readACL", user.getRoles());
    }

    public static Bson roleWriteAcl(User user) {
        return Filters.in("writeACL", user.getRoles());
    }

    public static Bson isPublic() {

        return Filters.or(Filters.and(
                        Filters.eq("readACL", new ArrayList<>()),
                        Filters.eq("writeACL", new ArrayList<>())),
                Filters.and(
                        Filters.eq("readACL", "*"),
                        Filters.eq("writeACL", "*"))
        );
    }

    public static Bson allAcl(User user) {

        return Filters.or(
                roleReadAcl(user),
                roleWriteAcl(user),
                readAcl(user),
                isPublic(),
                writeAcl(user)
        );
    }
}
