package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import entities.User;
import models.CreateAccountResponse;
import models.GetUserAccountsResponse;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest {
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

    @DisplayName("User can make a deposit")
    @Test
    public void userCanMakeDeposit() {
        double deposit = 5000;

        User user = AdminSteps.createUser();
        CreateAccountResponse createAccountResponse  = UserSteps.createAccount(user.getRequest());
        String accountNumber = createAccountResponse.getAccountNumber();

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

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        $(".account-selector").selectOptionContainingText(accountNumber);

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue("5000");

        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ Successfully deposited $5000 to account");

        alert.accept();

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        $(".account-selector").selectOptionContainingText(accountNumber);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accountNumber))
                .shouldHave(text("(Balance: $5000.00)"));

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), createAccountResponse.getId());

        assertThat(account.getBalance())
                .isEqualTo(deposit);
    }

    @DisplayName("User cannot make a deposit more than max limit")
    @Test
    public void userCannotMakeDepositMoreThanMax() {
        User user = AdminSteps.createUser();
        CreateAccountResponse createAccountResponse  = UserSteps.createAccount(user.getRequest());
        String accountNumber = createAccountResponse.getAccountNumber();

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

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        $(".account-selector").selectOptionContainingText(accountNumber);

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue("5001");

        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Please deposit less or equal to 5000$.");

        alert.accept();

        $(".account-selector").selectOptionContainingText(accountNumber);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accountNumber))
                .shouldHave(text("(Balance: $0.00)"));

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), createAccountResponse.getId());

        assertThat(account.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot make a negative deposit")
    @Test
    public void userCannotMakeNegativeDeposit() {
        User user = AdminSteps.createUser();
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        String accountNumber = createAccountResponse.getAccountNumber();

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

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        $(".account-selector").selectOptionContainingText(accountNumber);

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue("-100");

        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Please enter a valid amount.");

        alert.accept();

        $(".account-selector").selectOptionContainingText(accountNumber);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accountNumber))
                .shouldHave(text("(Balance: $0.00)"));

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), createAccountResponse.getId());

        assertThat(account.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot make a zero deposit")
    @Test
    public void userCannotMakeZeroDeposit() {
        User user = AdminSteps.createUser();
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        String accountNumber = createAccountResponse.getAccountNumber();

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

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        $(".account-selector").selectOptionContainingText(accountNumber);

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue("0");

        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Please enter a valid amount.");

        alert.accept();

        $(".account-selector").selectOptionContainingText(accountNumber);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accountNumber))
                .shouldHave(text("(Balance: $0.00)"));

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), createAccountResponse.getId());

        assertThat(account.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot make a zero deposit")
    @Test
    public void userCannotMakeDepositWithoutChoosingAccount() {
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

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Please select an account.");
    }
}
