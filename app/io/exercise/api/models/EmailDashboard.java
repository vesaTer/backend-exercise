package io.exercise.api.models;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


@Data
@BsonDiscriminator(key = "type", value = "email")
public class EmailDashboard extends DashboardContent {
    @NotNull
    String text;
    @NotNull
    String email;
    String subject;
//    @NotNull
//    List<String> readACL = new ArrayList<>();
//    @NotNull
//    List<String> writeACL = new ArrayList<>();

    @Override
    public DashboardType getType() {
        return DashboardType.EMAIL;
    }
}
