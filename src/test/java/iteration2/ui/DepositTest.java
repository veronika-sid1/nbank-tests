package iteration2.ui;

import api.entities.User;
import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.GetUserAccountsResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import base.BaseUITest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.UserDashboard;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest extends BaseUITest {
    @DisplayName("User can make a deposit")
    @Test
    public void userCanMakeDeposit() {
        double amount = RandomData.getRandomValidDepositAmount();
        User user = AdminSteps.createUser();
        CreateAccountResponse createAccountResponse  = UserSteps.createAccount(user.getRequest());
        String accountNumber = createAccountResponse.getAccountNumber();

        authAsUser(user.getRequest());

        new UserDashboard().open().enterDepositPage();

        DepositPage depositPage = new DepositPage();

        depositPage.selectAccount(accountNumber)
                .setAmount(amount).saveDeposit()
                .checkAlertMessageAndAccountIdAndAccept(
                        BankAlert.SUCCESSFUL_DEPOSIT.getMessage(), accountNumber);

        depositPage.open().selectAccount(accountNumber)
                        .assertSelectedAccount(accountNumber, amount);

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), createAccountResponse.getId());

        assertThat(account.getBalance())
                .isEqualTo(amount);
    }

    private static Stream<Arguments> invalidDepositValues() {
        return Stream.of(
                Arguments.of(5001, BankAlert.EXCEEDING_MAX_DEPOSIT),
                Arguments.of(-100, BankAlert.INVALID_AMOUNT),
                Arguments.of(0, BankAlert.INVALID_AMOUNT)
        );
    }

    @DisplayName("User cannot make an invalid deposit")
    @ParameterizedTest(name = "Invalid deposit: {0}")
    @MethodSource("invalidDepositValues")
    public void userCannotMakeInvalidDeposits(double amount, BankAlert error) {
        User user = AdminSteps.createUser();
        CreateAccountResponse createAccountResponse  = UserSteps.createAccount(user.getRequest());
        String accountNumber = createAccountResponse.getAccountNumber();

        authAsUser(user.getRequest());

        DepositPage depositPage = new DepositPage();

        depositPage.open().selectAccount(accountNumber)
                .setAmount(amount).saveDeposit()
                .checkAlertMessageAndAccept(error.getMessage());

        depositPage.open().selectAccount(accountNumber)
                .assertSelectedAccount(accountNumber, RequestSpecs.INITIAL_BALANCE);

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), createAccountResponse.getId());

        assertThat(account.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot make a deposit without choosing an account")
    @Test
    public void userCannotMakeDepositWithoutChoosingAccount() {
        User user = AdminSteps.createUser();
        CreateAccountResponse createAccountResponse  = UserSteps.createAccount(user.getRequest());

        authAsUser(user.getRequest());

        new DepositPage().open().saveDeposit()
                        .checkAlertMessageAndAccept(BankAlert.ACCOUNT_NOT_SELECTED.getMessage());

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), createAccountResponse.getId());

        assertThat(account.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }
}
