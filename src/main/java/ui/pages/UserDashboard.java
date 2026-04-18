package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement transferButton = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));
    private SelenideElement profile = $(Selectors.byClassName("user-name"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

    public UserDashboard enterDepositPage() {
        depositButton.click();
        return this;
    }

    public UserDashboard enterTransferPage() {
        transferButton.click();
        return this;
    }

    public UserDashboard enterProfilePage() {
        profile.click();
        return this;
    }

    public UserDashboard checkUsernameOnDashboardPage(String name) {
        welcomeText.shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, " + name));
        return this;
    }
}
