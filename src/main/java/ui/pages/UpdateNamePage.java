package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;
import lombok.Getter;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class UpdateNamePage extends BasePage<UpdateNamePage> {
    private SelenideElement nameField = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));
    private SelenideElement profileUsername = $(Selectors.byClassName("user-name"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public UpdateNamePage fillName(String name) {
        RetryUtils.retry(
                () -> {
                    nameField.click();
                    nameField.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                    nameField.sendKeys(Keys.DELETE);
                    nameField.sendKeys(name);
                    return nameField.getValue();
                },
                value -> value.contains(name),
                5,
                1000
        );
        nameField.shouldHave(exactValue(name));
        return this;
    }

    public UpdateNamePage waitNameLoaded(String currentName) {
        nameField.shouldBe(visible, enabled, interactable)
                .shouldHave(value(currentName));

        return this;
    }

    public UpdateNamePage waitNameEmpty() {
        nameField.shouldBe(visible, enabled, interactable)
                .shouldBe(empty);

        return this;
    }

    public UpdateNamePage saveChanges() {
        saveChangesButton.shouldBe(visible, enabled, interactable).click();
        return this;
    }

    public UpdateNamePage checkName(String name) {
        profileUsername.shouldHave(text(name));
        return this;
    }
}
