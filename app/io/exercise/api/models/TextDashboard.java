package io.exercise.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotNull;


@Data
@BsonDiscriminator(key = "type", value = "TEXT")
@EqualsAndHashCode(callSuper = true)

public class
TextDashboard extends DashboardContent{
    @NotNull
    private String text;
    @Override
    public DashboardType getType() {
        return DashboardType.TEXT;
    }
}
