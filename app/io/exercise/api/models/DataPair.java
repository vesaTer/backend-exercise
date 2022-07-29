package io.exercise.api.models;

import javax.validation.constraints.NotNull;

public class DataPair {
    @NotNull
    private String category;
    @NotNull
    private Integer value;
}
