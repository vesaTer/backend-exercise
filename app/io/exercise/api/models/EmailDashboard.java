package io.exercise.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotNull;

@Data
@BsonDiscriminator(key = "type", value = "EMAIL")
@EqualsAndHashCode(callSuper = true)

public class EmailDashboard extends DashboardContent {
    @NotNull
    String text;
    @NotNull
    String email;
    String subject;

    @Override
    public DashboardType getType() {
        return DashboardType.EMAIL;
    }
}
