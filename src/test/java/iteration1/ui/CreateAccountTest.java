package iteration1.ui;

import api.models.CreateUserRequest;
import api.models.GetUserAccountsResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import base.BaseUITest;
import common.annotations.Browsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ui.pages.BasePage.authAsUser;

public class CreateAccountTest extends BaseUITest {
    @Test
    @UserSession
    @Browsers({"firefox"})
    public void userCanCreateAccountTest() {
        new UserDashboard().open().createNewAccount();

        List<GetUserAccountsResponse> createdAccounts = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccounts).hasSize(1);

//        new UserDashboard().checkAlertMessageAndAccept(
//                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();
    }
}