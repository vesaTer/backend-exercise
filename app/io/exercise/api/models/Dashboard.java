package io.exercise.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.w3c.dom.stylesheets.LinkStyle;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Dashboard extends BaseModel{
  @NotNull
  @Size(min=3, max=20)
  private String name;
  @NotNull
  @Size(min=20)
  private String description;

  private String parentId;

  private Long createdAt = getCreatedAt();
  List<DashboardContent> items=new ArrayList<>();

}
