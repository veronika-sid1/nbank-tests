package ui.elements;

import com.codeborne.selenide.SelenideElement;

import java.util.Locale;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class AccountSelect extends BaseElement {

    public AccountSelect() {
        super($("select.account-selector"));
    }

    public AccountSelect(SelenideElement element) {
        super(element);
    }

    public AccountSelect selectAccount(String accountNumber) {
        element.selectOptionContainingText(accountNumber);
        return this;
    }

    //можно вынести с использованием дженерика при количестве страниц использования >2
    //public abstract class AccountSelectablePage<T extends AccountSelectablePage<T>> extends BasePage<T>
    public AccountSelect shouldHaveSelectedAccount(String accountNumber, double amount) {
        String formattedAmount = String.format(Locale.US, "%.2f", amount);

        element.getSelectedOption()
                .shouldHave(text(accountNumber + " (Balance: $" + formattedAmount + ")"));

        return this;
    }
}
