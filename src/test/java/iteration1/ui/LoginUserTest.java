package iteration1.ui;

import base.BaseUITest;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import api.models.CreateUserRequest;
import common.annotations.Browsers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.pages.AdminPanel;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;

import java.util.Map;

import static com.codeborne.selenide.Selenide.$;

public class LoginUserTest extends BaseUITest {
    @Test
    @Browsers({"chrome"})
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();

        new LoginPage().open().login(admin.getUsername(), admin.getPassword())
                .getPage(AdminPanel.class).getAdminPanelText().shouldBe(Condition.visible);
    }

    @Test
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = AdminSteps.createUser().getRequest();

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                        .getPage(UserDashboard.class).getWelcomeText()
                        .shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));
    }
}
