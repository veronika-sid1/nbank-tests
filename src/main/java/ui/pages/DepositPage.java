package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class DepositPage extends BasePage<DepositPage> {
    private SelenideElement amountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage selectAccount(String accountNumber) {
        accountSelector.selectOptionContainingText(accountNumber);
        return this;
    }

    public DepositPage setAmount(double amount) {
        amountField.setValue(String.valueOf(amount));
        return this;
    }

    public DepositPage saveDeposit() {
        depositButton.click();
        return this;
    }
}
