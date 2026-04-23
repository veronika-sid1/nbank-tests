package iteration1.api;

import api.entities.User;
import base.BaseTest;
import org.junit.jupiter.api.Test;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

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