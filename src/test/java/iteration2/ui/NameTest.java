package iteration2.ui;

import com.codeborne.selenide.*;
import entities.User;
import generators.RandomData;
import models.GetProfileResponse;
import models.LoginUserRequest;
import models.UpdateProfileRequest;
import models.UpdateProfileResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class NameTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.0.168:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @DisplayName("User can specify his name")
    @Test
    public void userCanSpecifyName() {
        String name = "Katya Ivanova";

        User user = AdminSteps.createUser();

        Selenide.open("/");

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(user.getRequest().getUsername())
                        .password(user.getRequest().getPassword()).build())
                .extract()
                .header("Authorization");

        $("body").shouldBe(visible);

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byClassName("user-name")).click();

        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(visible, enabled);

        input.shouldBe(empty);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(name);
        input.shouldHave(value(name));

        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ Name updated successfully!");

        alert.accept();

        refresh();

        $(Selectors.byClassName("user-name"))
                .shouldHave(text(name));

        $(Selectors.byText("\uD83C\uDFE0 Home")).click();

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, " + name));

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isEqualTo(name);
    }

    @DisplayName("User can edit name")
    @Test
    public void userCanEditName() {
        String name = "Katya Ivanova";
        String editedName = "Katya Fedorova";

        User user = AdminSteps.createUser();

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();

        UserSteps.updateUserName(user.getRequest(), updateProfileRequest);

        Selenide.open("/");

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(user.getRequest().getUsername())
                        .password(user.getRequest().getPassword()).build())
                .extract()
                .header("Authorization");

        $("body").shouldBe(visible);

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byClassName("user-name")).click();

        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(visible, enabled);

        input.shouldHave(value(name));
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(editedName);
        input.shouldHave(value(editedName));

        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ Name updated successfully!");

        alert.accept();

        refresh();

        $(Selectors.byClassName("user-name"))
                .shouldHave(text(editedName));

        $(Selectors.byText("\uD83C\uDFE0 Home")).click();

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, " + editedName));

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isEqualTo(editedName);
    }

    @DisplayName("User cannot specify invalid name")
    @Test
    public void userCannotSpecifyInvalidName() {
        String name = "AnnaPavlova123";
        String noname = "Noname";

        User user = AdminSteps.createUser();

        Selenide.open("/");

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(user.getRequest().getUsername())
                        .password(user.getRequest().getPassword()).build())
                .extract()
                .header("Authorization");

        $("body").shouldBe(visible);

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byClassName("user-name")).click();

        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(visible, enabled);

        input.shouldBe(empty);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(name);
        input.shouldHave(value(name));

        input.shouldHave(value(name));

        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("Name must contain two words with letters only");

        alert.accept();

        refresh();

        $(Selectors.byClassName("user-name"))
                .shouldHave(text(noname));

        $(Selectors.byText("\uD83C\uDFE0 Home")).click();

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isNull();
    }

    @DisplayName("User cannot save empty name")
    @Test
    public void userCannotSaveEmptyName() {
        User user = AdminSteps.createUser();

        Selenide.open("/");

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(user.getRequest().getUsername())
                        .password(user.getRequest().getPassword()).build())
                .extract()
                .header("Authorization");

        $("body").shouldBe(visible);

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byClassName("user-name")).click();

        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(visible, enabled);

        input.shouldBe(empty);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);

        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Please enter a valid name.");

        alert.accept();
        assertThat(user.getResponse().getName())
                .isNull();
    }

    @DisplayName("User cannot save already saved name")
    @Test
    public void userCannotSaveAlreadySavedName() {
        String name = "Katya Ivanova";
        User user = AdminSteps.createUser();

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();

        UserSteps.updateUserName(user.getRequest(), updateProfileRequest);

        Selenide.open("/");

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(user.getRequest().getUsername())
                        .password(user.getRequest().getPassword()).build())
                .extract()
                .header("Authorization");

        $("body").shouldBe(visible);

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byClassName("user-name")).click();

        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(visible, enabled);

        input.shouldHave(value(name));
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(name);
        input.shouldHave(value(name));

        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("New name is the same as the current one.");

        alert.accept();

        refresh();

        $(Selectors.byClassName("user-name"))
                .shouldHave(text(name));

        $(Selectors.byText("\uD83C\uDFE0 Home")).click();

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, " + name));

        GetProfileResponse getProfileResponse = UserSteps.getProfile(user.getRequest());

        assertThat(getProfileResponse.getName())
                .isEqualTo(name);
    }
}
