package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import ui.elements.AccountSelect;
import ui.elements.AlertPopup;

import static com.codeborne.selenide.Selenide.$;

public class DepositPage extends BasePage<DepositPage> {
    private final AccountSelect accountSelect = new AccountSelect();

    private SelenideElement amountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage setAmount(double amount) {
        amountField.setValue(String.valueOf(amount));
        return this;
    }

    public DepositPage saveDeposit() {
        depositButton.click();
        return this;
    }

    public DepositPage assertSelectedAccount(String accountNumber, double amount) {
        accountSelect.shouldHaveSelectedAccount(accountNumber, amount);
        return this;
    }

    public DepositPage selectAccount(String accountNumber) {
        accountSelect.selectAccount(accountNumber);
        return this;
    }
}
