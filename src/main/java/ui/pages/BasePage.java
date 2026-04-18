package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.Alert;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Locale;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage> {
    public abstract String url();

    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
    protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder", "Password"));
    protected SelenideElement accountSelector = $(".account-selector");
    protected SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home"));

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }

    public T checkAlertMessageAndAccept(String bankAlert) {
        Alert alert = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.alertIsPresent());

        String actualText = alert.getText();

        assertThat(actualText).contains(bankAlert);
        alert.accept();
        return (T) this;
    }

    public T checkAlertMessageAndAccountIdAndAccept(String bankAlert, String accId) {
        Alert alert = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.alertIsPresent());

        String actualText = alert.getText();

        assertThat(actualText).contains(bankAlert);
        assertThat(actualText).contains(accId);
        alert.accept();
        return (T) this;
    }

    public T assertSelectedAccount(String accountNumber, double amount) {
        String formattedAmount = String.format(Locale.US, "%.2f", amount);

        $(".account-selector")
                .getSelectedOption()
                .shouldHave(text(accountNumber + " (Balance: $" + formattedAmount + ")"));

        return (T) this;
    }

    public T returnToMainPage() {
        homeButton.click();
        return (T) this;
    }
}
