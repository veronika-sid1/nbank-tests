package iteration2.ui;

import api.entities.User;
import api.generators.RandomData;
import api.generators.UserRequestGenerator;
import api.models.GetProfileResponse;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;
import base.BaseUITest;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.elements.AlertPopup;
import ui.pages.BankAlert;
import ui.pages.UpdateNamePage;
import ui.pages.UserDashboard;

import static com.codeborne.selenide.Selenide.refresh;
import static org.assertj.core.api.Assertions.assertThat;

public class NameTest extends BaseUITest {
    @DisplayName("User can specify his name")
    @UserSession
    @Test
    public void userCanSpecifyName() {
        String name = RandomData.getName();

        User user = SessionStorage.getUser();

        UpdateNamePage updateNamePage = new UpdateNamePage();
        UserDashboard userDashboard = new UserDashboard();

        userDashboard.open().enterProfilePage();

        updateNamePage.waitPageLoaded().fillName(name).saveChanges(name);

        new AlertPopup()
                .checkAlertMessage(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .acceptAlert();

        refresh();

        updateNamePage.checkName(name).returnToMainPage();
        userDashboard.checkUsernameOnDashboardPage(name);

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isEqualTo(name);
    }

    @DisplayName("User can edit name")
    @UserSession
    @Test
    public void userCanEditName() {
        String nameReq = RandomData.getName();
        String editedNameReq = RandomData.getName();

        User user = SessionStorage.getUser();

        UserSteps.updateUserName(user.getRequest(), UserRequestGenerator.requestWithName(nameReq));

        UpdateNamePage updateNamePage = new UpdateNamePage();

        updateNamePage.open().waitNameLoaded(nameReq).fillName(editedNameReq).saveChanges();

        new AlertPopup()
                .checkAlertMessage(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .acceptAlert();

        refresh();

        updateNamePage.checkName(editedNameReq).returnToMainPage();

        new UserDashboard().checkUsernameOnDashboardPage(editedNameReq);

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isEqualTo(editedNameReq);
    }

    @DisplayName("User cannot specify invalid name")
    @UserSession
    @Test
    public void userCannotSpecifyInvalidName() {
        String invalidName = RandomData.getRandomInvalidName();

        User user = SessionStorage.getUser();

        UpdateNamePage updateNamePage = new UpdateNamePage();
        updateNamePage.open().waitPageLoaded().waitNameEmpty().fillName(invalidName).saveChanges();

        new AlertPopup()
                .checkAlertMessage(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS.getMessage())
                .acceptAlert();

        refresh();

        updateNamePage.checkName(ResponseSpecs.NONAME).returnToMainPage();

        new UserDashboard().checkUsernameOnDashboardPage(ResponseSpecs.NONAME.toLowerCase());

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isNull();
    }

    @DisplayName("User cannot save empty name")
    @UserSession
    @Test
    public void userCannotSaveEmptyName() {
        User user = SessionStorage.getUser();

        UpdateNamePage updateNamePage = new UpdateNamePage();

        updateNamePage.open().saveChanges();

        new AlertPopup()
                .checkAlertMessage(BankAlert.ENTER_VALID_NAME.getMessage())
                .acceptAlert();

        refresh();

        updateNamePage.checkName(ResponseSpecs.NONAME).returnToMainPage();

        new UserDashboard().checkUsernameOnDashboardPage(ResponseSpecs.NONAME);

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isNull();
    }

    @DisplayName("User cannot save already saved name")
    @UserSession
    @Test
    public void userCannotSaveAlreadySavedName() {
        String nameReq = RandomData.getName();

        User user = SessionStorage.getUser();

        UserSteps.updateUserName(user.getRequest(), UserRequestGenerator.requestWithName(nameReq));

        UpdateNamePage updateNamePage = new UpdateNamePage();

        updateNamePage.open().waitNameLoaded(nameReq).fillName(nameReq).saveChanges();

        new AlertPopup()
                .checkAlertMessage(BankAlert.NEW_NAME_SAME_AS_CURRENT.getMessage())
                .acceptAlert();

        refresh();

        updateNamePage.checkName(nameReq).returnToMainPage();

        new UserDashboard().checkUsernameOnDashboardPage(nameReq);
        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isEqualTo(nameReq);
    }
}
