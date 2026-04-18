package api.generators;

import api.models.UpdateProfileRequest;

public class UpdateProfileRequestGenerator extends UpdateProfileRequest {
    public static UpdateProfileRequest defaultUpdateProfileRequest(String name) {
        return UpdateProfileRequest.builder()
                .name(RandomData.getName())
                .build();
    }
}
