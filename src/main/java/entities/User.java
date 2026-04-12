package entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.CreateUserRequest;
import models.CreateUserResponse;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private CreateUserRequest request;
    private CreateUserResponse response;
}
