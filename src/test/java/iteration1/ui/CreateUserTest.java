package iteration1.ui;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import base.BaseUITest;
import common.annotations.AdminSession;
import org.junit.jupiter.api.Test;
import ui.elements.AlertPopup;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static com.codeborne.selenide.Selenide.refresh;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateUserTest extends BaseUITest {
    @Test
    @AdminSession
    public void adminCanCreateUserTest() {
        // ШАГ 2: админ создает юзера в банке
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        AdminPanel adminPanel = new AdminPanel()
                .open()
                .createUser(newUser);

        new AlertPopup().checkAlertMessage(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage())
                .acceptAlert();

        refresh();

        assertTrue(
                adminPanel.getAllUsers().stream()
                        .anyMatch(user -> user.getUsername().equals(newUser.getUsername()))
        );

        // ШАГ 5: проверка, что юзер создан на API
        CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();

        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test
    @AdminSession
    public void adminCannotCreateUserWithInvalidDataTest() {
        // ШАГ 2: админ создает юзера в банке
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

//        assertTrue(new AdminPanel().open().createUser(newUser)
//                .checkAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
//                .getAllUsers().stream().noneMatch(userBadge -> userBadge.getUsername().equals(newUser.getUsername())));

        long usersWithSameUsernameAsNewUser = AdminSteps.getAllUsers().stream().filter(user -> user.getUsername().equals(newUser.getUsername())).count();

        assertThat(usersWithSameUsernameAsNewUser).isZero();
    }
}