package ui.elements;

import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.Alert;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class AlertPopup {
    private final String text;

    public AlertPopup() {
        Alert alert = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.alertIsPresent());

        this.text = alert.getText();
    }

    public AlertPopup checkAlertMessage(String bankAlert) {
        assertThat(text).contains(bankAlert);
        return this;
    }

    public AlertPopup checkAccountId(String accId) {
        assertThat(text).contains(accId);
        return this;
    }

    public AlertPopup acceptAlert() {
        new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.alertIsPresent())
                .accept();
        return this;
    }
}
