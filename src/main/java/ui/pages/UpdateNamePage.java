package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Condition.value;
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
        nameField.shouldBe(visible, enabled);

        nameField.click();
        nameField.clear();
        nameField.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        nameField.sendKeys(Keys.DELETE);
        nameField.sendKeys(name);
        nameField.shouldHave(value(name));

        return this;
    }

    public UpdateNamePage saveChanges() {
        saveChangesButton.click();
        return this;
    }

    public UpdateNamePage checkName(String name) {
        profileUsername.shouldHave(text(name));
        return this;
    }
}
