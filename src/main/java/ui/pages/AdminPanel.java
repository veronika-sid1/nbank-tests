package ui.pages;

import api.models.CreateUserRequest;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import ui.elements.UserBadge;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

@Getter
public class AdminPanel extends BasePage<AdminPanel> {
    private SelenideElement adminPanelText = $(byText("Admin Panel"));
    private SelenideElement addUserButton = $(byText("Add User"));

    @Override
    public String url() {
        return "/admin";
    }

    public AdminPanel createUser(String username, String password) {
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        addUserButton.click();
        return this;
    }

    public AdminPanel createUser(CreateUserRequest createUserRequest) {
        createUser(createUserRequest.getUsername(), createUserRequest.getPassword());
        return this;
    }

    public List<UserBadge> getAllUsers() {
        ElementsCollection elementsCollection =  $(byText("All Users")).parent().findAll("li");
        return generatePageElements(elementsCollection, UserBadge::new);
    }

    public UserBadge findUserByUsername(String username) {
        SelenideElement userElement = $x("//li[contains(., '" + username + "')]")
                .shouldBe(visible, Duration.ofSeconds(8));

        return new UserBadge(userElement);
    }

    public AdminPanel waitUntilLoaded() {
        $(byText("All Users")).shouldBe(visible, Duration.ofSeconds(8));
        return this;
    }
}
