package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;
import lombok.Getter;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

@Getter
public class UpdateNamePage extends BasePage<UpdateNamePage> {
    private SelenideElement nameField = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));
    private SelenideElement profileUsername = $(Selectors.byClassName("user-name"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public UpdateNamePage waitPageLoaded() {
        nameField.shouldBe(visible, enabled);
        saveChangesButton.shouldBe(visible);
        return this;
    }

//    public UpdateNamePage fillName(String name) {
//        RetryUtils.retry(
//                () -> {
//                    nameField.shouldBe(visible, enabled, interactable)
//                            .click();
//
//                    nameField.setValue(name);
//                    return nameField.getValue();
//                },
//                value -> value.contains(name),
//                10,
//                1000
//        );
//        nameField.shouldHave(exactValue(name));
//        return this;
//    }

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

    public UpdateNamePage saveChanges(String expectedName) {
        nameField.shouldHave(exactValue(expectedName));

        saveChangesButton.shouldBe(visible, enabled).click();

        return this;
    }

    public UpdateNamePage fillName(String name) {
        nameField.shouldBe(visible, enabled).click();

        RetryUtils.retry(
                () -> {
                    nameField.sendKeys(Keys.CONTROL + "a");
                    nameField.sendKeys(Keys.BACK_SPACE);
                    nameField.sendKeys(name);
                    nameField.pressTab();

                    sleep(300);

                    return nameField.getValue();
                },
                actual -> name.equals(actual),
                5,
                500
        );

        return this;
    }
}
