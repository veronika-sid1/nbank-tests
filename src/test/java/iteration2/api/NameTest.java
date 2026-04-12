package iteration2.api;

import entities.User;
import generators.RandomData;
import models.ErrorResponse;
import models.GetProfileResponse;
import models.UpdateProfileRequest;
import models.UpdateProfileResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.ResponseSpecs;

public class NameTest extends BaseTest {

    @DisplayName("User can specify name")
    @Test
    public void userCanSpecifyName() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(RandomData.getName())
                .build();

        UpdateProfileResponse updateProfileResponse = UserSteps.updateUserName(user.getRequest(), updateProfileRequest);

        softly.assertThat(updateProfileResponse.getCustomer().getName())
                .isEqualTo(updateProfileRequest.getName());
        softly.assertThat(updateProfileResponse.getMessage())
                .isEqualTo(ResponseSpecs.PROFILE_UPDATED);

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        softly.assertThat(getProfileResponse.getName())
                        .isEqualTo(updateProfileRequest.getName());
    }

    @DisplayName("User can edit name")
    @Test
    public void userCanEditName() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(RandomData.getName())
                .build();

        UpdateProfileResponse updateProfileResponse = UserSteps.updateUserName(user.getRequest(), updateProfileRequest);

        softly.assertThat(updateProfileResponse.getCustomer().getName())
                .isEqualTo(updateProfileRequest.getName());
        softly.assertThat(updateProfileResponse.getMessage())
                .isEqualTo(ResponseSpecs.PROFILE_UPDATED);

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        softly.assertThat(getProfileResponse.getName())
                .isEqualTo(updateProfileRequest.getName());

        UpdateProfileRequest editedProfileRequest = UpdateProfileRequest.builder()
                .name(RandomData.getName())
                .build();

        UpdateProfileResponse updateProfileResponseSecond = UserSteps.updateUserName(user.getRequest(), editedProfileRequest);

        softly.assertThat(updateProfileResponseSecond.getCustomer().getName())
                .isEqualTo(editedProfileRequest.getName());
        softly.assertThat(updateProfileResponseSecond.getMessage())
                .isEqualTo(ResponseSpecs.PROFILE_UPDATED);

        GetProfileResponse getProfileResponseSecond = UserSteps.getProfile(user.getRequest());

        softly.assertThat(getProfileResponseSecond.getName())
                .isEqualTo(editedProfileRequest.getName());
    }

    @DisplayName("User cannot enter invalid name")
    @ParameterizedTest(name = "User cannot enter invalid name: {0}")
    @ValueSource(strings = {"Katya", "Anna Petrova1", "", "Anna  Pavlova", "Anna Pavlova Ivanova", "Annas!#123"})
    public void userCannotSpecifyInvalidNames(String name) {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();

        ErrorResponse errorResponse = UserSteps.attemptUpdateUsernameUsingInvalidData(user.getRequest(), updateProfileRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(ResponseSpecs.NAME_VALIDATION);

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        softly.assertThat(getProfileResponse.getName())
                .isNull();
    }

    @DisplayName("Unauthorized user cannot specify name")
    @Test
    public void unauthorizedUserCannotSpecifyName() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(RandomData.getName())
                .build();

        UserSteps.updateUserNameUnauthorized(updateProfileRequest);

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        softly.assertThat(getProfileResponse.getName())
                .isNull();
    }

    @DisplayName("User with invalid auth cannot specify name")
    @Test
    public void invalidAuthUserCannotSpecifyName() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(RandomData.getName())
                .build();

        UserSteps.updateUserNameWithBrokenAuth(updateProfileRequest);

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        softly.assertThat(getProfileResponse.getName())
                .isNull();
    }
}
