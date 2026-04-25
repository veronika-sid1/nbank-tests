package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import common.utils.RetryUtils;
import ui.elements.AccountSelect;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

public class TransferPage extends BasePage<TransferPage> {
    private final AccountSelect accountSelect = new AccountSelect();

    private SelenideElement recipientField = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private SelenideElement recipientAccountNumberField = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement amountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement detailsConfirmationCheckbox = $(Selectors.byId("confirmCheck"));
    private SelenideElement sendTransferButton = $(Selectors.byText("🚀 Send Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage setRecipientName(String name) {
        recipientField.setValue(name);
        return this;
    }

    public TransferPage setRecipientAccountNumber(String accountNumber) {
        recipientAccountNumberField.setValue(accountNumber);
        return this;
    }

    public TransferPage setRecipientAccountNumberId(String accountNumber) {
        recipientAccountNumberField.setValue(accountNumber.replaceAll("\\D+", ""));
        return this;
    }

    public TransferPage setAmount(double amount) {
        amountField.setValue(String.valueOf(amount));
        return this;
    }

    public TransferPage confirmDetails() {
        detailsConfirmationCheckbox.shouldBe(visible, enabled);

        if (!detailsConfirmationCheckbox.isSelected()) {
            detailsConfirmationCheckbox.click();
        }

        detailsConfirmationCheckbox.shouldBe(checked);
        return this;
    }

    public TransferPage saveTransfer() {
        sendTransferButton.shouldBe(visible, enabled, interactable).click();
        return this;
    }

    public TransferPage assertSelectedAccount(String accountNumber, double amount) {
        accountSelect.shouldHaveSelectedAccount(accountNumber, amount);
        return this;
    }

    //если будет продолжать флакать при нахождении аккаунта в выпадающем списке
    public TransferPage waitAccountVisible(String accountNumber) {
        accountSelect.waitOptionVisible(accountNumber);
        return this;
    }

    public TransferPage selectAccount(String accountNumber) {
        accountSelect.selectAccount(accountNumber);
        RetryUtils.retry(
                () -> accountSelect.selectAccount(accountNumber),
                value -> value != null,
                3,
                1000
        );

        accountSelect.shouldHaveSelectedAccountOnlyAccCheck(accountNumber);
        return this;
    }

    //для исследования падения
    public TransferPage printTransferFormState() {
        System.out.println("URL = " + WebDriverRunner.url());
        System.out.println("SELECTED ACCOUNT = " + accountSelect);
        System.out.println("RECIPIENT NAME = " + recipientField.getValue());
        System.out.println("RECIPIENT ACCOUNT = " + recipientAccountNumberField.getValue());
        System.out.println("AMOUNT = " + amountField.getValue());
        System.out.println("CHECKBOX SELECTED = " + detailsConfirmationCheckbox.isSelected());
        System.out.println("SAVE ENABLED = " + sendTransferButton.isEnabled());
        return this;
    }
}
