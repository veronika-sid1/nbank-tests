package iteration2.ui;

import api.entities.User;
import api.generators.RandomData;
import api.generators.RandomModelGenerator;
import api.models.GetProfileResponse;
import api.models.UpdateProfileRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;
import base.BaseUITest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UpdateNamePage;
import ui.pages.UserDashboard;

import static com.codeborne.selenide.Selenide.refresh;
import static org.assertj.core.api.Assertions.assertThat;

public class NameTest extends BaseUITest {
    @DisplayName("User can specify his name")
    @Test
    public void userCanSpecifyName() {
        String name = RandomData.getName();

        User user = AdminSteps.createUser();

        authAsUser(user.getRequest());

        UpdateNamePage updateNamePage = new UpdateNamePage();
        UserDashboard userDashboard = new UserDashboard();

        userDashboard.open().enterProfilePage();

        updateNamePage.fillName(name).saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

        refresh();

        updateNamePage.checkName(name).returnToMainPage();
        userDashboard.checkUsernameOnDashboardPage(name);

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isEqualTo(name);
    }

    @DisplayName("User can edit name")
    @Test
    public void userCanEditName() {
        UpdateProfileRequest nameReq = RandomModelGenerator.generate(UpdateProfileRequest.class);
        UpdateProfileRequest editedNameReq = RandomModelGenerator.generate(UpdateProfileRequest.class);

        User user = AdminSteps.createUser();

        UserSteps.updateUserName(user.getRequest(), nameReq);

        authAsUser(user.getRequest());

        UpdateNamePage updateNamePage = new UpdateNamePage();

        updateNamePage.open().fillName(editedNameReq.getName()).saveChanges()
                        .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

        refresh();

        updateNamePage.checkName(editedNameReq.getName()).returnToMainPage();

        new UserDashboard().checkUsernameOnDashboardPage(editedNameReq.getName());

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isEqualTo(editedNameReq.getName());
    }

    @DisplayName("User cannot specify invalid name")
    @Test
    public void userCannotSpecifyInvalidName() {
        String invalidName = "AnnaPavlova123";

        User user = AdminSteps.createUser();

        authAsUser(user.getRequest());

        UpdateNamePage updateNamePage = new UpdateNamePage();

        updateNamePage.open().fillName(invalidName).saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS.getMessage());

        refresh();

        updateNamePage.checkName(ResponseSpecs.NONAME).returnToMainPage();

        new UserDashboard().checkUsernameOnDashboardPage(ResponseSpecs.NONAME);

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isNull();
    }

    @DisplayName("User cannot save empty name")
    @Test
    public void userCannotSaveEmptyName() {
        User user = AdminSteps.createUser();

        authAsUser(user.getRequest());

        UpdateNamePage updateNamePage = new UpdateNamePage();

        updateNamePage.open().saveChanges()
                .checkAlertMessageAndAccept(BankAlert.ENTER_VALID_NAME.getMessage());

        refresh();

        updateNamePage.checkName(ResponseSpecs.NONAME).returnToMainPage();

        new UserDashboard().checkUsernameOnDashboardPage(ResponseSpecs.NONAME);

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isNull();
    }

    @DisplayName("User cannot save already saved name")
    @Test
    public void userCannotSaveAlreadySavedName() {
        UpdateProfileRequest nameReq = RandomModelGenerator.generate(UpdateProfileRequest.class);

        User user = AdminSteps.createUser();

        UserSteps.updateUserName(user.getRequest(), nameReq);

        authAsUser(user.getRequest());

        UpdateNamePage updateNamePage = new UpdateNamePage();

        updateNamePage.open().fillName(nameReq.getName()).saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NEW_NAME_SAME_AS_CURRENT.getMessage());

        refresh();

        updateNamePage.checkName(nameReq.getName()).returnToMainPage();

        new UserDashboard().checkUsernameOnDashboardPage(nameReq.getName());
        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isEqualTo(nameReq.getName());
    }
}
