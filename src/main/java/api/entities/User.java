package api.entities;

import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private CreateUserRequest request;
    private CreateUserResponse response;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return response != null && user.response != null &&
                response.getId() == user.response.getId();
    }

    @Override
    public int hashCode() {
        if (response == null) {
            throw new IllegalStateException("User must have response before using as key");
        }
        return Long.hashCode(response.getId());
    }
}
