package iteration2.ui;

import api.entities.User;
import api.generators.DepositRequestGenerator;
import api.generators.RandomData;
import api.generators.UserRequestGenerator;
import api.helpers.TestHelpers;
import api.models.CreateAccountResponse;
import api.models.GetUserAccountsResponse;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import base.BaseUITest;
import com.codeborne.selenide.WebDriverRunner;
import common.annotations.UserAccount;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.elements.AlertPopup;
import ui.pages.BankAlert;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

import static com.codeborne.selenide.Selenide.refresh;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferTest extends BaseUITest {
    @DisplayName("User can transfer money between his accounts")
    @UserSession
    @UserAccount(value = 2)
    @Test
    public void userCanTransferMoneyBetweenHisAccounts() {
        double transferAmount = RandomData.getRandomValidDepositAmount();
        User user = SessionStorage.getUser();

        CreateAccountResponse account = SessionStorage.getAccount(1);
        CreateAccountResponse accountSecond = SessionStorage.getAccount(2);

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator
                .withAmount(account.getId(), transferAmount));

        new UserDashboard().open().enterTransferPage();

        TransferPage transferPage = new TransferPage();

        transferPage.selectAccount(account.getAccountNumber())
                        .setRecipientAccountNumber(accountSecond.getAccountNumber())
                        .setAmount(transferAmount).confirmDetails().saveTransfer();

        new AlertPopup()
                .checkAlertMessage(BankAlert.SUCCESSFUL_TRANSFER.getMessage())
                .checkAccountId(accountSecond.getAccountNumber())
                .acceptAlert();

        refresh();

        transferPage.waitAccountVisible(account.getAccountNumber()).selectAccount(account.getAccountNumber())
                .assertSelectedAccount(account.getAccountNumber(), RequestSpecs.INITIAL_BALANCE);

        transferPage.waitAccountVisible(account.getAccountNumber()).selectAccount(accountSecond.getAccountNumber())
                .assertSelectedAccount(accountSecond.getAccountNumber(), transferAmount);

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accountSecond.getId());

        assertThat(firstAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        assertThat(secondAccount.getBalance())
                .isEqualTo(transferAmount);
    }

    @DisplayName("User can transfer money to another user's account")
    @UserSession(value = 2)
    @UserAccount(value = 2)
    @Test
    public void userCanTransferMoneyToAnotherUsersAccount() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = SessionStorage.getUser(1);
        User userSecond = SessionStorage.getUser(2);

        CreateAccountResponse account = SessionStorage.getAccount(1, 1);
        CreateAccountResponse accountSecond = SessionStorage.getAccount(2, 1);

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(account.getId(), transferAmount));

        new TransferPage().open().selectAccount(account.getAccountNumber()).setRecipientName(RandomData.getName())
                .setRecipientAccountNumber(accountSecond.getAccountNumber()).setAmount(transferAmount)
                .confirmDetails().saveTransfer();

        new AlertPopup()
                .checkAlertMessage(BankAlert.SUCCESSFUL_TRANSFER.getMessage())
                .checkAccountId(accountSecond.getAccountNumber())
                .acceptAlert();

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(userSecond.getRequest(), accountSecond.getId());

        assertThat(firstAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        assertThat(secondAccount.getBalance())
                .isEqualTo(transferAmount);
    }

    @DisplayName("User cannot transfer money without filling all fields of transfer form")
    @UserSession(value = 2)
    @UserAccount(value = 2)
    @Test
    public void userCannotTransferMoneyIfNotAllFieldsFilled() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = SessionStorage.getUser(1);
        User userSecond = SessionStorage.getUser(2);

        CreateAccountResponse account = SessionStorage.getAccount(1, 1);
        CreateAccountResponse accountSecond = SessionStorage.getAccount(2, 1);

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(account.getId(), transferAmount));

        new TransferPage().open().selectAccount(account.getAccountNumber()).saveTransfer();

        new AlertPopup()
                .checkAlertMessage(BankAlert.ALL_FIELDS_MUST_BE_FILLED.getMessage())
                .acceptAlert();

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(userSecond.getRequest(), accountSecond.getId());

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot transfer money to the same account")
    @UserSession
    @UserAccount
    @Test
    public void userCannotTransferToSameAccount() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = SessionStorage.getUser();
        CreateAccountResponse account = SessionStorage.getAccount(1);

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(account.getId(), transferAmount));

        new TransferPage().open().selectAccount(account.getAccountNumber()).setRecipientName(RandomData.getName())
                .setRecipientAccountNumberId(account.getAccountNumber()).setAmount(transferAmount)
                .confirmDetails().saveTransfer();

        new AlertPopup()
                .checkAlertMessage(BankAlert.CANNOT_TRANSFER_TO_SAME_ACCOUNT.getMessage())
                .acceptAlert();

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), account.getId());

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
    }

    @DisplayName("User cannot transfer money to non-existent account")
    @UserSession
    @UserAccount
    @Test
    public void userCannotTransferToNonExistentAccount() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = SessionStorage.getUser();
        CreateAccountResponse account = SessionStorage.getAccount(1);

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(account.getId(), transferAmount));

        new TransferPage().open().selectAccount(account.getAccountNumber()).setRecipientName(RandomData.getName())
                .setRecipientAccountNumberId(String.valueOf(Integer.MAX_VALUE)).setAmount(RequestSpecs.MAX_DEPOSIT)
                .confirmDetails().saveTransfer();

        new AlertPopup()
                .checkAlertMessage(BankAlert.NO_USER_WITH_THIS_ACCOUNT.getMessage())
                .acceptAlert();

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), account.getId());

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
    }

    @DisplayName("User cannot transfer less than min limit")
    @UserSession
    @UserAccount(value = 2)
    @Test
    public void userCannotTransferLessThanMinLimit() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = SessionStorage.getUser();

        CreateAccountResponse account = SessionStorage.getAccount(1, 1);
        CreateAccountResponse accountSecond = SessionStorage.getAccount(1, 2);

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(account.getId(), transferAmount));

        new TransferPage().open().waitAccountVisible(account.getAccountNumber())
                .selectAccount(account.getAccountNumber()).setRecipientName(RandomData.getName())
                .setRecipientAccountNumber(accountSecond.getAccountNumber()).setAmount(RequestSpecs.NEGATIVE_AMOUNT)
                .confirmDetails().saveTransfer();

        System.out.println("URL AFTER SAVE = " + WebDriverRunner.url());
        System.out.println("PAGE AFTER SAVE = " + WebDriverRunner.source());

        new AlertPopup()
                .checkAlertMessage(BankAlert.TRANSFER_MUST_BE_AT_LEAST_0_01.getMessage())
                .acceptAlert();

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accountSecond.getId());

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot transfer more than max limit")
    @UserSession
    @UserAccount(value = 2)
    @Test
    public void userCannotTransferMoreThanMaxLimit() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = SessionStorage.getUser();

        CreateAccountResponse account = SessionStorage.getAccount(1, 1);
        CreateAccountResponse accountSecond = SessionStorage.getAccount(1, 2);

        TestHelpers.repeat(3, () -> UserSteps.makeDeposit(user.getRequest(),
                DepositRequestGenerator.withAmount(account.getId(), transferAmount)));

        new TransferPage().open().selectAccount(account.getAccountNumber()).setRecipientName(RandomData.getName())
                .setRecipientAccountNumber(accountSecond.getAccountNumber()).setAmount(RequestSpecs.EXCEEDING_TRANSFER)
                .confirmDetails().printTransferFormState().saveTransfer();

        new AlertPopup()
                .checkAlertMessage(BankAlert.TRANSFER_CANNOT_EXCEED_MAX_LIMIT.getMessage())
                .acceptAlert();

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accountSecond.getId());

        assertThat(firstAccount.getBalance())
                .isEqualTo(3 * transferAmount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot make transfer with invalid recipient name")
    @UserSession
    @UserAccount(value = 2)
    @Test
    public void userCannotMakeTransferWithInvalidRecipientName() {
        String name = RandomData.getName();
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = SessionStorage.getUser();

        CreateAccountResponse account = SessionStorage.getAccount(1, 1);
        CreateAccountResponse accountSecond = SessionStorage.getAccount(1, 2);

        UserSteps.updateUserName(user.getRequest(), UserRequestGenerator.requestWithName(name));
        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(account.getId(), transferAmount));

        new TransferPage().open().selectAccount(account.getAccountNumber()).setRecipientName(RandomData.getName())
                .setRecipientAccountNumber(accountSecond.getAccountNumber()).setAmount(transferAmount)
                .confirmDetails().printTransferFormState().saveTransfer();

        new AlertPopup()
                .checkAlertMessage(BankAlert.INCORRECT_RECIPIENT_NAME.getMessage())
                .acceptAlert();

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accountSecond.getId());

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot make transfer without confirming details")
    @UserSession
    @UserAccount(value = 2)
    @Test
    public void userCannotMakeTransferWithoutConfirmingDetails() {
        String name = RandomData.getName();
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = SessionStorage.getUser();

        CreateAccountResponse account = SessionStorage.getAccount(1, 1);
        CreateAccountResponse accountSecond = SessionStorage.getAccount(1, 2);

        UserSteps.updateUserName(user.getRequest(), UserRequestGenerator.requestWithName(name));
        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(account.getId(), transferAmount));

        new TransferPage().open().selectAccount(account.getAccountNumber()).setRecipientName(RandomData.getName())
                .setRecipientAccountNumber(accountSecond.getAccountNumber()).setAmount(transferAmount)
                .saveTransfer();

        new AlertPopup()
                .checkAlertMessage(BankAlert.ALL_FIELDS_MUST_BE_FILLED.getMessage())
                .acceptAlert();

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accountSecond.getId());

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot transfer more than amount on sender's balance")
    @UserSession
    @UserAccount(value = 2)
    @Test
    public void userCannotTransferMoreThanExistingBalance() {
        double transferAmount = RandomData.getRandomValidDepositAmount();
        double exceedingTransferAmount = RandomData.getRandomTransferMoreThanDeposit(transferAmount);

        User user = SessionStorage.getUser();

        CreateAccountResponse account = SessionStorage.getAccount(1, 1);
        CreateAccountResponse accountSecond = SessionStorage.getAccount(1, 2);

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(account.getId(), transferAmount));

        new TransferPage().open().waitAccountVisible(account.getAccountNumber())
                .selectAccount(account.getAccountNumber()).setRecipientName(RandomData.getName())
                .setRecipientAccountNumber(accountSecond.getAccountNumber()).setAmount(exceedingTransferAmount)
                .confirmDetails().saveTransfer();

        new AlertPopup()
                .checkAlertMessage(BankAlert.INSUFFICIENT_FUNDS_INVALID_ACC.getMessage())
                .acceptAlert();

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accountSecond.getId());

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }
}
