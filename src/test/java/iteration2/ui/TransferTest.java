package iteration2.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import entities.User;
import generators.RandomData;
import helpers.TestHelpers;
import models.*;
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
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferTest {
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

    @DisplayName("User can transfer money between his accounts")
    @Test
    public void userCanTransferMoneyBetweenHisAccounts() {
        double amount = 5000;
        double amountToTransfer = 5000;

        User user = AdminSteps.createUser();
        CreateAccountResponse account =  UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond =  UserSteps.createAccount(user.getRequest());
        long accIdSecondLong = accountSecond.getId();
        String accIdSecondString = accountSecond.getAccountNumber();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accIdLong)
                .balance(amount)
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

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

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue("Kate Ivanova");

        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(accIdSecondString);

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amountToTransfer));

        $(Selectors.byId("confirmCheck")).setSelected(true);

        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ Successfully transferred $5000 to account");
        assertThat(alertText).contains(accIdSecondString);

        alert.accept();

        refresh();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accIdString))
                .shouldHave(text("(Balance: $0.00)"));

        $(".account-selector").selectOptionContainingText(accIdSecondString);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accIdSecondString))
                .shouldHave(text("(Balance: $5000.00)"));

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accIdSecondLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        assertThat(secondAccount.getBalance())
                .isEqualTo(amount);
    }

    @DisplayName("User can transfer money to another user's account")
    @Test
    public void userCanTransferMoneyToAnotherUsersAccount() {
        double amount = 5000;
        double transferAmount = 2000;
        String recipientName = "Ivan Ivanov";

        User user = AdminSteps.createUser();
        User userSecond = AdminSteps.createUser();

        CreateAccountResponse account =  UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond =  UserSteps.createAccount(userSecond.getRequest());
        long accIdSecondLong = accountSecond.getId();
        String accIdSecondString = accountSecond.getAccountNumber();

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(RandomData.getName())
                .build();

        UserSteps.updateUserName(user.getRequest(), updateProfileRequest);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accIdLong)
                .balance(amount)
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

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

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue(recipientName);

        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(accIdSecondString);

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(transferAmount));

        $(Selectors.byId("confirmCheck")).setSelected(true);

        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ Successfully transferred $2000.0 to account")
                .contains(accIdSecondString);

        alert.accept();

        refresh();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accIdString))
                .shouldHave(text("(Balance: $3000.00)"));

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(userSecond.getRequest(), accIdSecondLong);

        assertThat(firstAccount.getBalance())
                .isCloseTo(amount - transferAmount, within(0.01));
        assertThat(secondAccount.getBalance())
                .isEqualTo(transferAmount);
    }

    @DisplayName("User cannot transfer money without filling all fields of transfer form")
    @Test
    public void userCannotTransferMoneyIfNotAllFieldsFilled() {
        User user = AdminSteps.createUser();
        UserSteps.createAccount(user.getRequest());

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

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Please fill all fields and confirm.");

        alert.accept();
    }

    @DisplayName("User cannot transfer money to the same account")
    @Test
    public void userCannotTransferToSameAccount() {
        double amount = 5000;

        User user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accIdLong)
                .balance(amount)
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

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

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue("Kate Ivanova");

        String number = accIdString.replaceAll("\\D+", "");

        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(number);

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));

        $(Selectors.byId("confirmCheck")).setSelected(true);

        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ You cannot transfer money to the same account.");

        alert.accept();

        refresh();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accIdString))
                .shouldHave(text("(Balance: $5000.00)"));

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(amount);
    }

    @DisplayName("User cannot transfer money to non-existent account")
    @Test
    public void userCannotTransferToNonExistentAccount() {
        double amount = 5000;
        String recipientName = "Ivan Ivanov";

        User user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accIdLong)
                .balance(amount)
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

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

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(".account-selector").selectOptionContainingText(accIdString);

        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue(recipientName);

        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(String.valueOf(Integer.MAX_VALUE));

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));

        $(Selectors.byId("confirmCheck")).setSelected(true);

        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ No user found with this account number.");

        alert.accept();

        refresh();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accIdString))
                .shouldHave(text("(Balance: $5000.00)"));

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(amount);
    }

    @DisplayName("User cannot transfer less than min limit")
    @Test
    public void userCannotTransferLessThanMinLimit() {
        double amount = 5000;
        double negativeAmount = -100;
        String recipientName = "Ivan Ivanov";

        User user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();
        CreateAccountResponse accountSecond = UserSteps.createAccount(user.getRequest());
        long accIdLongSecond = accountSecond.getId();
        String accIdStringSecond = accountSecond.getAccountNumber();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accIdLong)
                .balance(amount)
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

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

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(".account-selector").selectOptionContainingText(accIdString);

        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue(recipientName);

        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(accIdStringSecond);

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(negativeAmount));

        $(Selectors.byId("confirmCheck")).setSelected(true);

        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Error: Transfer amount must be at least 0.01");

        alert.accept();

        refresh();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accIdString))
                .shouldHave(text("(Balance: $5000.00)"));

        $(".account-selector").selectOptionContainingText(accIdStringSecond);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accIdStringSecond))
                .shouldHave(text("(Balance: $0.00)"));

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accIdLongSecond);

        assertThat(firstAccount.getBalance())
                .isEqualTo(amount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot transfer more than max limit")
    @Test
    public void userCannotTransferMoreThanMaxLimit() {
        double amount = 5000;
        double moreThanMaxLimit = 10001;
        String recipientName = "Ivan Ivanov";

        User user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();
        CreateAccountResponse accountSecond = UserSteps.createAccount(user.getRequest());
        long accIdLongSecond = accountSecond.getId();
        String accIdStringSecond = accountSecond.getAccountNumber();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accIdLong)
                .balance(amount)
                .build();

        TestHelpers.repeat(3, () -> UserSteps.makeDeposit(user.getRequest(), depositRequest));

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

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(".account-selector").selectOptionContainingText(accIdString);

        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue(recipientName);

        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(accIdStringSecond);

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(moreThanMaxLimit));

        $(Selectors.byId("confirmCheck")).setSelected(true);

        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Error: Transfer amount cannot exceed 10000");

        alert.accept();

        refresh();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accIdString))
                .shouldHave(text("(Balance: $15000.00)"));

        $(".account-selector").selectOptionContainingText(accIdStringSecond);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accIdStringSecond))
                .shouldHave(text("(Balance: $0.00)"));

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accIdLongSecond);

        assertThat(firstAccount.getBalance())
                .isEqualTo(amount * 3);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot make transfer with invalid recipient name")
    @Test
    public void userCannotMakeTransferWithInvalidRecipientName() {
        double amount = 5000;
        String name = "Katya Ivanova";

        User user = AdminSteps.createUser();
        CreateAccountResponse account =  UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond =  UserSteps.createAccount(user.getRequest());
        long accIdSecondLong = accountSecond.getId();
        String accIdSecondString = accountSecond.getAccountNumber();

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();

        UserSteps.updateUserName(user.getRequest(), updateProfileRequest);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accIdLong)
                .balance(amount)
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

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

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue("Ivan Ivanov");

        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(accIdSecondString);

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));

        $(Selectors.byId("confirmCheck")).setSelected(true);

        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ The recipient name does not match the registered name.");

        alert.accept();

        refresh();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accIdString))
                .shouldHave(text("(Balance: $5000.00)"));

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accIdSecondLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(amount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot make transfer without confirming details")
    @Test
    public void userCannotMakeTransferWithoutConfirmingDetails() {
        double amount = 5000;
        String name = "Katya Ivanova";

        User user = AdminSteps.createUser();
        CreateAccountResponse account =  UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond =  UserSteps.createAccount(user.getRequest());
        long accIdSecondLong = accountSecond.getId();
        String accIdSecondString = accountSecond.getAccountNumber();

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();

        UserSteps.updateUserName(user.getRequest(), updateProfileRequest);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accIdLong)
                .balance(amount)
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

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

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue(name);

        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(accIdSecondString);

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));


        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Please fill all fields and confirm.");

        alert.accept();

        refresh();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accIdString))
                .shouldHave(text("(Balance: $5000.00)"));

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accIdSecondLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(amount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot transfer more than amount on sender's balance")
    @Test
    public void userCannotTransferMoreThanExistingBalance() {
        double amount = 5000;

        User user = AdminSteps.createUser();
        CreateAccountResponse account =  UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond =  UserSteps.createAccount(user.getRequest());
        long accIdSecondLong = accountSecond.getId();
        String accIdSecondString = accountSecond.getAccountNumber();

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

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(".account-selector").selectOptionContainingText(accIdString);

        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue("Kate Ivanova");

        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(accIdSecondString);

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));

        $(Selectors.byId("confirmCheck")).setSelected(true);

        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Error: Invalid transfer: insufficient funds or invalid accounts");

        alert.accept();

        refresh();

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accIdSecondLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }
}
