package io.exercise.api.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dashboard extends BaseModel {
    @NotNull
    @Size(min = 3, max = 20)
    private String name;
    @NotNull
    @Size(min = 20)
    public String description;

    public ObjectId parentId;

    @BsonIgnore
    List<DashboardContent> items = new ArrayList<>();

    @BsonProperty("children")
    List<Dashboard> children;
    @JsonIgnore
    @BsonProperty("level")
    Integer level;

    public Dashboard(String name, String description){
        this.name=name;
        this.description=description;
    }


}
