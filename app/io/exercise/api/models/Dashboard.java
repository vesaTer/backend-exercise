package io.exercise.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard extends BaseModel{
  private String name;
  private String description;
  private String parentId;
  private Long createdAt = getCreatedAt();
  private List<String> readACL = new ArrayList<>();
  private List<String> writeACL = new ArrayList<>();
}
