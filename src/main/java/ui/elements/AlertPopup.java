package ui.elements;

import com.codeborne.selenide.WebDriverRunner;
import common.utils.RetryUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class AlertPopup {
    private final String text;
    private final Alert alert;

    public AlertPopup() {
        this.alert = RetryUtils.retry(
                () -> {
                    try {
                        return WebDriverRunner.getWebDriver().switchTo().alert();
                    } catch (NoAlertPresentException e) {
                        return null;
                    }
                },
                Objects::nonNull,
                5,
                1000
        );

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
        alert.accept();
        return this;
    }
}
