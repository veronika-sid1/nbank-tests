package api.models;

import api.configs.Config;
import api.generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest extends BaseModel {
    @GeneratingRule(regexp = "^[A-Za-z0-9]{3,15}$")
    private String username;
    @GeneratingRule(regexp = "^[A-Z]{3}[a-z]{4}[0-9]{3}[$%&]{2}$")
    private String password;
    @GeneratingRule(regexp = "^USER$")
    private String role;

    public static CreateUserRequest getAdmin() {
        return CreateUserRequest.builder()
                .username(Config.getProperty("admin.login"))
                .password(Config.getProperty("admin.password"))
                .build();
    }
}
