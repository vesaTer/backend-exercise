package io.exercise.api.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public @Data class User extends BaseModel {
    @NotEmpty
    @Size(min = 4)
    private String username;

    @NotEmpty
    @Size(min = 6)
    private String password;
    private List<String> roles = new ArrayList<>();

    public boolean canWrite(BaseModel b) {
        for (String role : roles) {
            if (b.getWriteACL().contains(role)) {
                return true;
            }
        }
        return b.getWriteACL().contains(id.toString());
    }

    public boolean canRead(BaseModel b) {
        for (String role : roles) {
            if (b.getReadACL().contains(role)) {
                return true;
            }
        }
        return b.getReadACL().contains(id.toString());
    }
}
