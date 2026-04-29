package iteration2.ui;

import api.entities.User;
import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.GetUserAccountsResponse;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import base.BaseUITest;
import common.annotations.APIVersion;
import common.annotations.UserAccount;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.elements.AlertPopup;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.UserDashboard;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest extends BaseUITest {
    @APIVersion("with_validation_fix")
    @DisplayName("User can make a deposit")
    @UserSession
    @UserAccount
    @Test
    public void userCanMakeDeposit() {
        double amount = RandomData.getRandomValidDepositAmount();
        User user = SessionStorage.getUser();
        CreateAccountResponse createAccountResponse = SessionStorage.getAccount(1);
        String accountNumber = createAccountResponse.getAccountNumber();

        new UserDashboard().open().enterDepositPage();

        DepositPage depositPage = new DepositPage();

        depositPage.selectAccount(accountNumber)
                .setAmount(amount).saveDeposit();

        new AlertPopup().checkAlertMessage(BankAlert.SUCCESSFUL_DEPOSIT.getMessage())
                        .checkAccountId(accountNumber)
                        .acceptAlert();

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

    @APIVersion("with_validation_fix")
    @DisplayName("User cannot make an invalid deposit")
    @UserSession
    @UserAccount
    @ParameterizedTest(name = "Invalid deposit: {0}")
    @MethodSource("invalidDepositValues")
    public void userCannotMakeInvalidDeposits(double amount, BankAlert error) {
        User user = SessionStorage.getUser();
        CreateAccountResponse createAccountResponse = SessionStorage.getAccount(1);
        String accountNumber = createAccountResponse.getAccountNumber();

        DepositPage depositPage = new DepositPage();

        depositPage.open().selectAccount(accountNumber)
                .setAmount(amount).saveDeposit();

        new AlertPopup().checkAlertMessage(error.getMessage())
                        .acceptAlert();

        depositPage.open().selectAccount(accountNumber)
                .assertSelectedAccount(accountNumber, RequestSpecs.INITIAL_BALANCE);

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), createAccountResponse.getId());

        assertThat(account.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @APIVersion("with_validation_fix")
    @DisplayName("User cannot make a deposit without choosing an account")
    @UserSession
    @UserAccount
    @Test
    public void userCannotMakeDepositWithoutChoosingAccount() {
        User user = SessionStorage.getUser();
        CreateAccountResponse createAccountResponse  = SessionStorage.getAccount(1);

        new DepositPage().open().saveDeposit();

        new AlertPopup().checkAlertMessage(BankAlert.ACCOUNT_NOT_SELECTED.getMessage())
                        .acceptAlert();

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), createAccountResponse.getId());

        assertThat(account.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }
}
