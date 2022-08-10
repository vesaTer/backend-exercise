package io.exercise.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotNull;

@Data
@BsonDiscriminator(key = "type", value = "IMAGE")
@EqualsAndHashCode(callSuper = true)

public class ImageDashboard extends DashboardContent{
    @NotNull
    private String url;
    @Override
    public DashboardType getType() {
        return DashboardType.IMAGE;
    }
}
