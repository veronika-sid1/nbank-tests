package iteration1.api;

import entities.User;
import iteration2.api.BaseTest;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        User user = AdminSteps.createUser();

        new CrudRequester(RequestSpecs.authAsUser(user.getRequest().getUsername(), user.getRequest().getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post();
    }
}