package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class TransferPage extends BasePage<TransferPage> {
    private SelenideElement recipientField = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private SelenideElement recipientAccountNumberField = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement amountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement detailsConfirmationCheckbox = $(Selectors.byId("confirmCheck"));
    private SelenideElement sendTransferButton = $(Selectors.byText("🚀 Send Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage selectAccount(String accountNumber) {
        accountSelector.selectOptionContainingText(accountNumber);
        return this;
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
        detailsConfirmationCheckbox.setSelected(true);
        return this;
    }

    public TransferPage saveTransfer() {
        sendTransferButton.click();
        return this;
    }
}
