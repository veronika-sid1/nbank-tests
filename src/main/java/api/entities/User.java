package api.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private CreateUserRequest request;
    private CreateUserResponse response;
}
