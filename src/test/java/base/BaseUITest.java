package base;

import api.configs.Config;
import api.entities.User;
import api.models.CreateAccountResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Configuration;
import common.extensions.AdminSessionExtension;
import common.extensions.BrowserMatchExtension;
import common.extensions.UserAccountExtension;
import common.extensions.UserSessionExtension;
import common.storage.SessionStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Selenide.closeWebDriver;

@ExtendWith(AdminSessionExtension.class)
@ExtendWith(UserSessionExtension.class)
@ExtendWith(UserAccountExtension.class)
@ExtendWith(BrowserMatchExtension.class)
public class BaseUITest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = Config.getProperty("uiRemote");
        Configuration.baseUrl = Config.getProperty("uiBaseUrl");
        Configuration.browser = Config.getProperty("browser");
        Configuration.browserSize = Config.getProperty("browserSize");

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @AfterEach
    void tearDownUi() {
        deleteUiEntities();
        SessionStorage.clear();
        closeWebDriver();
    }

    private void deleteUiEntities() {
        for (User user : SessionStorage.getUsers()) {
            List<CreateAccountResponse> accounts = SessionStorage.getAllAccountsForUser(user);

            if (accounts != null) {
                for (CreateAccountResponse account : accounts) {
                    try {
                        UserSteps.deleteAccount(account.getId(), user.getRequest());
                    } catch (Exception e) {
                        System.out.println("Failed to delete account " + account.getId());
                    }
                }
            }
        }

        for (User user : SessionStorage.getUsers()) {
            try {
                AdminSteps.deleteUser(user.getResponse().getId());
            } catch (Exception e) {
                System.out.println("Failed to delete user " + user.getResponse().getId());
            }
        }
    }
}
