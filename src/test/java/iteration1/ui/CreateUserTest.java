package iteration1.ui;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import base.BaseUITest;
import common.annotations.APIVersion;
import common.annotations.AdminSession;
import org.junit.jupiter.api.Test;
import ui.elements.AlertPopup;
import ui.elements.UserBadge;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static com.codeborne.selenide.Selenide.refresh;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateUserTest extends BaseUITest {
    @APIVersion("with_validation_fix")
    @Test
    @AdminSession
    public void adminCanCreateUserTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        new AdminPanel().open();

        new AdminPanel().createUser(newUser.getUsername(), newUser.getPassword());

        new AlertPopup()
                .checkAlertMessage(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage())
                .acceptAlert();

        refresh();

        UserBadge newUserBadge = new AdminPanel()
                .waitUntilLoaded()
                .findUserByUsername(newUser.getUsername());

        CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst()
                .get();

        assertThat(newUserBadge)
                .as("UserBadge should exist on Dashboard after user creation")
                .isNotNull();

        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @APIVersion("with_validation_fix")
    @Test
    @AdminSession
    public void adminCannotCreateUserWithInvalidDataTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword());

        new AlertPopup().checkAlertMessage(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
                .acceptAlert();

        assertTrue(new AdminPanel().getAllUsers().stream().noneMatch(userBadge -> userBadge.getUsername().equals(newUser.getUsername())));

        long usersWithSameUsernameAsNewUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername())).count();

        assertThat(usersWithSameUsernameAsNewUser).isZero();
    }
}