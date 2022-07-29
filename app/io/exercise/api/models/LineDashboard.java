package io.exercise.api.models;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import java.util.ArrayList;
import java.util.List;

@Data
@BsonDiscriminator(key = "type", value = "line")
public class LineDashboard extends DashboardContent{

    private List<DataPair> dataPair = new ArrayList<>();
    @Override
    public DashboardType getType() {
        return DashboardType.LINE;
    }
}
