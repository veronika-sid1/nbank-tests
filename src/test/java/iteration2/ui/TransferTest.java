package iteration2.ui;

import api.entities.User;
import api.generators.DepositRequestGenerator;
import api.generators.RandomData;
import api.generators.UserRequestGenerator;
import api.helpers.TestHelpers;
import api.models.CreateAccountResponse;
import api.models.GetUserAccountsResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import base.BaseUITest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

import static com.codeborne.selenide.Selenide.refresh;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferTest extends BaseUITest {
    @DisplayName("User can transfer money between his accounts")
    @Test
    public void userCanTransferMoneyBetweenHisAccounts() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond = UserSteps.createAccount(user.getRequest());
        long accIdSecondLong = accountSecond.getId();
        String accIdSecondString = accountSecond.getAccountNumber();

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator
                .withAmount(accIdLong, transferAmount));

        authAsUser(user.getRequest());

        new UserDashboard().open().enterTransferPage();

        TransferPage transferPage = new TransferPage();

        transferPage.selectAccount(accIdString)
                        .setRecipientAccountNumber(accIdSecondString)
                        .setAmount(transferAmount).confirmDetails().saveTransfer()
                        .checkAlertMessageAndAccountIdAndAccept(
                                BankAlert.SUCCESSFUL_TRANSFER.getMessage(), accIdSecondString);

        refresh();

        transferPage.selectAccount(accIdString)
                .assertSelectedAccount(accIdString, RequestSpecs.INITIAL_BALANCE);

        transferPage.selectAccount(accIdSecondString)
                .assertSelectedAccount(accIdSecondString, transferAmount);

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accIdSecondLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        assertThat(secondAccount.getBalance())
                .isEqualTo(transferAmount);
    }

    @DisplayName("User can transfer money to another user's account")
    @Test
    public void userCanTransferMoneyToAnotherUsersAccount() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = AdminSteps.createUser();
        User userSecond = AdminSteps.createUser();

        CreateAccountResponse account =  UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond =  UserSteps.createAccount(userSecond.getRequest());
        long accIdSecondLong = accountSecond.getId();
        String accIdSecondString = accountSecond.getAccountNumber();

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(accIdLong, transferAmount));

        authAsUser(user.getRequest());

        new TransferPage().open().selectAccount(accIdString).setRecipientName(RandomData.getName())
                .setRecipientAccountNumber(accIdSecondString).setAmount(transferAmount)
                .confirmDetails().saveTransfer().checkAlertMessageAndAccountIdAndAccept(
                        BankAlert.SUCCESSFUL_TRANSFER.getMessage(), accIdSecondString);

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(userSecond.getRequest(), accIdSecondLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        assertThat(secondAccount.getBalance())
                .isEqualTo(transferAmount);
    }

    @DisplayName("User cannot transfer money without filling all fields of transfer form")
    @Test
    public void userCannotTransferMoneyIfNotAllFieldsFilled() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = AdminSteps.createUser();
        User userSecond = AdminSteps.createUser();

        CreateAccountResponse account =  UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond = UserSteps.createAccount(userSecond.getRequest());
        long accIdSecondLong = accountSecond.getId();

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(accIdLong, transferAmount));
        authAsUser(user.getRequest());

        new TransferPage().open().selectAccount(accIdString).saveTransfer().checkAlertMessageAndAccept(
                        BankAlert.ALL_FIELDS_MUST_BE_FILLED.getMessage());

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(userSecond.getRequest(), accIdSecondLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot transfer money to the same account")
    @Test
    public void userCannotTransferToSameAccount() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = AdminSteps.createUser();

        CreateAccountResponse account =  UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(accIdLong, transferAmount));

        authAsUser(user.getRequest());

        new TransferPage().open().selectAccount(accIdString).setRecipientName(RandomData.getName())
                .setRecipientAccountNumberId(accIdString).setAmount(transferAmount)
                .confirmDetails().saveTransfer().checkAlertMessageAndAccept(
                        BankAlert.CANNOT_TRANSFER_TO_SAME_ACCOUNT.getMessage());

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
    }

    @DisplayName("User cannot transfer money to non-existent account")
    @Test
    public void userCannotTransferToNonExistentAccount() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = AdminSteps.createUser();

        CreateAccountResponse account = UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(accIdLong, transferAmount));

        authAsUser(user.getRequest());

        new TransferPage().open().selectAccount(accIdString).setRecipientName(RandomData.getName())
                .setRecipientAccountNumberId(String.valueOf(Integer.MAX_VALUE)).setAmount(RequestSpecs.MAX_DEPOSIT)
                .confirmDetails().saveTransfer().checkAlertMessageAndAccept(
                        BankAlert.NO_USER_WITH_THIS_ACCOUNT.getMessage());

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
    }

    @DisplayName("User cannot transfer less than min limit")
    @Test
    public void userCannotTransferLessThanMinLimit() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond = UserSteps.createAccount(user.getRequest());
        long accIdLongSecond = accountSecond.getId();
        String accIdStringSecond = accountSecond.getAccountNumber();

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(accIdLong, transferAmount));
        authAsUser(user.getRequest());

        new TransferPage().open().selectAccount(accIdString).setRecipientName(RandomData.getName())
                .setRecipientAccountNumber(accIdStringSecond).setAmount(RequestSpecs.NEGATIVE_AMOUNT)
                .confirmDetails().saveTransfer().checkAlertMessageAndAccept(
                        BankAlert.TRANSFER_MUST_BE_AT_LEAST_0_01.getMessage());

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accIdLongSecond);

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot transfer more than max limit")
    @Test
    public void userCannotTransferMoreThanMaxLimit() {
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = AdminSteps.createUser();

        CreateAccountResponse account = UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond = UserSteps.createAccount(user.getRequest());
        long accIdLongSecond = accountSecond.getId();
        String accIdStringSecond = accountSecond.getAccountNumber();

        TestHelpers.repeat(3, () -> UserSteps.makeDeposit(user.getRequest(),
                DepositRequestGenerator.withAmount(accIdLong, transferAmount)));

        authAsUser(user.getRequest());

        new TransferPage().open().selectAccount(accIdString).setRecipientName(RandomData.getName())
                .setRecipientAccountNumber(accIdStringSecond).setAmount(RequestSpecs.EXCEEDING_TRANSFER)
                .confirmDetails().saveTransfer().checkAlertMessageAndAccept(
                        BankAlert.TRANSFER_CANNOT_EXCEED_MAX_LIMIT.getMessage());

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accIdLongSecond);

        assertThat(firstAccount.getBalance())
                .isEqualTo(3 * transferAmount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot make transfer with invalid recipient name")
    @Test
    public void userCannotMakeTransferWithInvalidRecipientName() {
        String name = RandomData.getName();
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = AdminSteps.createUser();
        CreateAccountResponse account =  UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond =  UserSteps.createAccount(user.getRequest());
        long accIdSecondLong = accountSecond.getId();
        String accIdSecondString = accountSecond.getAccountNumber();

        UserSteps.updateUserName(user.getRequest(), UserRequestGenerator.requestWithName(name));
        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(accIdLong, transferAmount));

        authAsUser(user.getRequest());

        new TransferPage().open().selectAccount(accIdString).setRecipientName(RandomData.getName())
                .setRecipientAccountNumber(accIdSecondString).setAmount(transferAmount)
                .confirmDetails().saveTransfer().checkAlertMessageAndAccept(
                        BankAlert.INCORRECT_RECIPIENT_NAME.getMessage());

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accIdSecondLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot make transfer without confirming details")
    @Test
    public void userCannotMakeTransferWithoutConfirmingDetails() {
        String name = RandomData.getName();
        double transferAmount = RandomData.getRandomValidDepositAmount();

        User user = AdminSteps.createUser();
        CreateAccountResponse account =  UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond =  UserSteps.createAccount(user.getRequest());
        long accIdSecondLong = accountSecond.getId();
        String accIdSecondString = accountSecond.getAccountNumber();

        UserSteps.updateUserName(user.getRequest(), UserRequestGenerator.requestWithName(name));

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(accIdLong, transferAmount));

        authAsUser(user.getRequest());

        new TransferPage().open().selectAccount(accIdString).setRecipientName(RandomData.getName())
                .setRecipientAccountNumber(accIdSecondString).setAmount(transferAmount)
                .saveTransfer().checkAlertMessageAndAccept(
                        BankAlert.ALL_FIELDS_MUST_BE_FILLED.getMessage());

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accIdSecondLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot transfer more than amount on sender's balance")
    @Test
    public void userCannotTransferMoreThanExistingBalance() {
        double transferAmount = RandomData.getRandomValidDepositAmount();
        double exceedingTransferAmount = RandomData.getRandomTransferMoreThanDeposit(transferAmount);

        User user = AdminSteps.createUser();
        CreateAccountResponse account =  UserSteps.createAccount(user.getRequest());
        long accIdLong = account.getId();
        String accIdString = account.getAccountNumber();

        CreateAccountResponse accountSecond =  UserSteps.createAccount(user.getRequest());
        long accIdSecondLong = accountSecond.getId();
        String accIdSecondString = accountSecond.getAccountNumber();

        UserSteps.makeDeposit(user.getRequest(), DepositRequestGenerator.withAmount(accIdLong, transferAmount));

        authAsUser(user.getRequest());

        new TransferPage().open().selectAccount(accIdString).setRecipientName(RandomData.getName())
                .setRecipientAccountNumber(accIdSecondString).setAmount(exceedingTransferAmount)
                .confirmDetails().saveTransfer().checkAlertMessageAndAccept(
                        BankAlert.INSUFFICIENT_FUNDS_INVALID_ACC.getMessage());

        GetUserAccountsResponse firstAccount = UserSteps.getAccountById(user.getRequest(), accIdLong);
        GetUserAccountsResponse secondAccount = UserSteps.getAccountById(user.getRequest(), accIdSecondLong);

        assertThat(firstAccount.getBalance())
                .isEqualTo(transferAmount);
        assertThat(secondAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }
}
