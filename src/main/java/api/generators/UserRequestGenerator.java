package api.generators;

import api.models.UpdateProfileRequest;

public class UserRequestGenerator extends UpdateProfileRequest {
    public static UpdateProfileRequest requestWithName(String name) {
        return UpdateProfileRequest.builder()
                .name(name)
                .build();
    }
}
