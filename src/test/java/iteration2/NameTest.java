package iteration2;

import generators.RandomData;
import io.restassured.http.ContentType;
import models.*;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AdminCreateUserRequester;
import requests.GetProfileRequester;
import requests.UpdateProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static io.restassured.RestAssured.given;

public class NameTest extends BaseTest {

    // positive: user can specify name
    @Test
    public void userCanSpecifyName() {
        String name = "Katya Smith";
        String expectedMessage = "Profile updated successfully";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();

        UpdateProfileResponse updateProfileResponse = new UpdateProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .put(updateProfileRequest).extract().as(UpdateProfileResponse.class);

        softly.assertThat(updateProfileResponse.getCustomer().getName())
                .isEqualTo(name);
        softly.assertThat(updateProfileResponse.getMessage())
                .isEqualTo(expectedMessage);

        GetProfileResponse customer = new GetProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetProfileResponse.class);

        softly.assertThat(customer.getName())
                        .isEqualTo(name);
    }

    // positive: user can edit name
    @Test
    public void userCanEditName() {
        String originalName = "Katya Smith";
        String editedName = "Ekaterina Smith";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(originalName)
                .build();

        new UpdateProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .put(updateProfileRequest);

        GetProfileResponse customer = new GetProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetProfileResponse.class);

        softly.assertThat(customer.getName())
                .isEqualTo(originalName);

        UpdateProfileRequest editedProfileRequest = UpdateProfileRequest.builder()
                .name(editedName)
                .build();

        new UpdateProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .put(editedProfileRequest);

        GetProfileResponse customerUpdated = new GetProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetProfileResponse.class);

        softly.assertThat(customerUpdated.getName())
                .isEqualTo(editedName);
    }

    //negative: user cannot enter one word for name
    //negative: user cannot enter digits in name
    //negative: user cannot enter empty name
    //negative: user cannot enter two spaces in name
    //negative: user cannot enter three words in name
    //negative: user cannot enter special characters in name
    @ParameterizedTest
    @ValueSource(strings = {"Katya", "Anna Petrova1", "", "Anna  Pavlova", "Anna Pavlova Ivanova", "Annas!#123"})
    public void userCannotSpecifyInvalidNames(String name) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();

        new UpdateProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest())
                .put(updateProfileRequest);

        GetProfileResponse customer = new GetProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetProfileResponse.class);

        softly.assertThat(customer.getName())
                .isNull();
    }

    // negative: unauthorized user cannot specify name
    @Test
    public void unauthorizedUserCannotSpecifyName() {
        String name = "Katya Smith";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();

        new UpdateProfileRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsUnauthorized())
                .put(updateProfileRequest);

        GetProfileResponse customer = new GetProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetProfileResponse.class);

        softly.assertThat(customer.getName())
                .isNull();
    }

    //negative: user with invalid auth cannot specify name
    @Test
    public void invalidAuthUserCannotSpecifyName() {
        String name = "Katya Smith";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();

        new UpdateProfileRequester(
                RequestSpecs.brokenAuthUserSpec(),
                ResponseSpecs.requestReturnsUnauthorized())
                .put(updateProfileRequest);

        GetProfileResponse customer = new GetProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetProfileResponse.class);

        softly.assertThat(customer.getName())
                .isNull();
    }
}
