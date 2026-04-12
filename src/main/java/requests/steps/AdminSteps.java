package requests.steps;

import entities.User;
import generators.RandomModelGenerator;
import io.qameta.allure.Step;
import models.CreateUserRequest;
import models.CreateUserResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class AdminSteps {
    @Step("Create user using admin account")
    public static User createUser() {
        CreateUserRequest request = RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse response = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(request);

         return new User(request, response);
    }

    @Step("Delete user")
    public static void deleteUser(long userId) {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.DELETE_USER,
                ResponseSpecs.requestReturnsOK())
                .delete(userId);
    }
}
