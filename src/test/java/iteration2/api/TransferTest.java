package iteration2.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.entities.User;
import api.generators.RandomData;
import api.helpers.TestHelpers;
import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import base.BaseTest;
import common.annotations.APIVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferTest extends BaseTest {
    @APIVersion("with_validation_fix")
    @DisplayName("User can transfer money between his accounts")
    @Test
    public void userCanTransferMoneyBetweenAccounts() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        long senderAccountId = UserSteps.createAccount(user.getRequest()).getId();
        long receiverAccountId = UserSteps.createAccount(user.getRequest()).getId();
        accountsToDelete.put(senderAccountId, user.getRequest());
        accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse beforeTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(beforeTransferSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(RandomData.getRandomValidTransferLessOrEqualDeposit(depositRequest.getBalance()))
                .build();

        TransferResponse transferResponse = UserSteps.makeTransfer(user.getRequest(), transferRequest);

        ModelAssertions.assertThatModels(transferRequest, transferResponse).match();

        softly.assertThat(transferResponse.getMessage()).isEqualTo(ResponseSpecs.TRANSFER_SUCCESSFUL);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isCloseTo(depositRequest.getBalance() - transferRequest.getAmount(), within(0.01));
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isCloseTo(RequestSpecs.INITIAL_BALANCE + transferRequest.getAmount(), within(0.01));
    }

    @APIVersion("with_database")
    @DisplayName("User can transfer money between his accounts")
    @Test
    public void userCanTransferMoneyBetweenAccountsWithDB() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long senderAccountId = createAccountResponse.getId();
        CreateAccountResponse createAccountResponseSecond = UserSteps.createAccount(user.getRequest());
        long receiverAccountId = createAccountResponseSecond.getId();
        //accountsToDelete.put(senderAccountId, user.getRequest());
        //accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeTransferSenderAccount, accountDao).match();

        GetUserAccountsResponse beforeTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDaoSecond = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeTransferReceiverAccount, accountDaoSecond).match();

        softly.assertThat(beforeTransferSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDaoAfterDepositSenderAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        AccountDao accountDaoAfterDepositReceiverAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());

        DaoAndModelAssertions.assertThat(afterDepositSenderAccount, accountDaoAfterDepositSenderAccount).match();
        DaoAndModelAssertions.assertThat(afterDepositReceiverAccount, accountDaoAfterDepositReceiverAccount).match();

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(RandomData.getRandomValidTransferLessOrEqualDeposit(depositRequest.getBalance()))
                .build();

        TransferResponse transferResponse = UserSteps.makeTransfer(user.getRequest(), transferRequest);

        AccountDao accountDaoAfterTransfer = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(transferResponse, accountDaoAfterTransfer).match();
        ModelAssertions.assertThatModels(transferRequest, transferResponse).match();

        AccountDao accountDaoAfterTransferSender = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        softly.assertThat(accountDaoAfterTransferSender.getBalance())
                .isCloseTo(depositRequest.getBalance() - transferRequest.getAmount(), within(0.01));
        softly.assertThat(transferResponse.getMessage()).isEqualTo(ResponseSpecs.TRANSFER_SUCCESSFUL);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        double accBalanceDB = DataBaseSteps.getBalanceByAccountNumber(accountDao.getAccountNumber());
        double accBalanceSecondDB = DataBaseSteps.getBalanceByAccountNumber(accountDaoSecond.getAccountNumber());

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isCloseTo(depositRequest.getBalance() - transferRequest.getAmount(), within(0.01));
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isCloseTo(RequestSpecs.INITIAL_BALANCE + transferRequest.getAmount(), within(0.01));

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(accBalanceDB);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(accBalanceSecondDB);
    }

    @APIVersion("with_validation_fix")
    @DisplayName("User can transfer money to someone else's account")
    @Test
    public void userCanTransferToAnotherUsersAccount() {
        User user = AdminSteps.createUser();
        User secondUser = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());
        usersToDelete.add(secondUser.getResponse().getId());

        long senderAccountUserId = UserSteps.createAccount(user.getRequest()).getId();
        long receiverAccountSecondUserId = UserSteps.createAccount(secondUser.getRequest()).getId();
        accountsToDelete.put(senderAccountUserId, user.getRequest());
        accountsToDelete.put(receiverAccountSecondUserId, secondUser.getRequest());

        GetUserAccountsResponse beforeTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountUserId);
        GetUserAccountsResponse beforeTransferReceiverAccount = UserSteps.getAccountById(secondUser.getRequest(), receiverAccountSecondUserId);

        softly.assertThat(beforeTransferSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountUserId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

            UserSteps.makeDeposit(user.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountUserId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(secondUser.getRequest(), receiverAccountSecondUserId);

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountUserId)
                .receiverAccountId(receiverAccountSecondUserId)
                .amount(RandomData.getRandomValidTransferLessOrEqualDeposit(depositRequest.getBalance()))
                .build();

        TransferResponse transferResponse = UserSteps.makeTransfer(user.getRequest(), transferRequest);

        ModelAssertions.assertThatModels(transferRequest, transferResponse).match();

        softly.assertThat(transferResponse.getMessage()).isEqualTo(ResponseSpecs.TRANSFER_SUCCESSFUL);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountUserId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(secondUser.getRequest(), receiverAccountSecondUserId);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isCloseTo(depositRequest.getBalance() - transferRequest.getAmount(), within(0.01));
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isCloseTo(RequestSpecs.INITIAL_BALANCE + transferRequest.getAmount(), within(0.01));
    }

    @APIVersion("with_database")
    @DisplayName("User can transfer money to someone else's account")
    @Test
    public void userCanTransferToAnotherUsersAccountWithDB() {
        User user = AdminSteps.createUser();
        User secondUser = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());
        usersToDelete.add(secondUser.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long senderAccountId = createAccountResponse.getId();
        CreateAccountResponse createAccountResponseSecond = UserSteps.createAccount(secondUser.getRequest());
        long receiverAccountId = createAccountResponseSecond.getId();
        //accountsToDelete.put(senderAccountUserId, user.getRequest());
        //accountsToDelete.put(receiverAccountSecondUserId, secondUser.getRequest());

        GetUserAccountsResponse beforeTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeTransferSenderAccount, accountDao).match();

        GetUserAccountsResponse beforeTransferReceiverAccount = UserSteps.getAccountById(secondUser.getRequest(), receiverAccountId);
        AccountDao accountDaoSecond = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeTransferReceiverAccount, accountDaoSecond).match();

        softly.assertThat(beforeTransferSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(secondUser.getRequest(), receiverAccountId);
        AccountDao accountDaoAfterDepositSenderAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        AccountDao accountDaoAfterDepositReceiverAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());

        DaoAndModelAssertions.assertThat(afterDepositSenderAccount, accountDaoAfterDepositSenderAccount).match();
        DaoAndModelAssertions.assertThat(afterDepositReceiverAccount, accountDaoAfterDepositReceiverAccount).match();

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(RandomData.getRandomValidTransferLessOrEqualDeposit(depositRequest.getBalance()))
                .build();

        TransferResponse transferResponse = UserSteps.makeTransfer(user.getRequest(), transferRequest);

        AccountDao accountDaoAfterTransfer = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(transferResponse, accountDaoAfterTransfer).match();
        ModelAssertions.assertThatModels(transferRequest, transferResponse).match();

        AccountDao accountDaoAfterTransferSender = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        softly.assertThat(accountDaoAfterTransferSender.getBalance())
                .isCloseTo(depositRequest.getBalance() - transferRequest.getAmount(), within(0.01));
        softly.assertThat(transferResponse.getMessage()).isEqualTo(ResponseSpecs.TRANSFER_SUCCESSFUL);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(secondUser.getRequest(), receiverAccountId);

        double accBalanceDB = DataBaseSteps.getBalanceByAccountNumber(accountDao.getAccountNumber());
        double accBalanceSecondDB = DataBaseSteps.getBalanceByAccountNumber(accountDaoSecond.getAccountNumber());

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isCloseTo(depositRequest.getBalance() - transferRequest.getAmount(), within(0.01));
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isCloseTo(RequestSpecs.INITIAL_BALANCE + transferRequest.getAmount(), within(0.01));

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(accBalanceDB);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(accBalanceSecondDB);
    }

    @APIVersion("with_validation_fix")
    @DisplayName("User can transfer valid amount")
    @ParameterizedTest(name = "User can transfer valid amount: {0}")
    @ValueSource(doubles = {10000.0, 0.01, 1.0, 0.02, 9999, 9999.99})
    public void userCanTransferValidAmountsBetweenAccounts(double transferAmount) {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        long senderAccountId = UserSteps.createAccount(user.getRequest()).getId();
        long receiverAccountId = UserSteps.createAccount(user.getRequest()).getId();
        accountsToDelete.put(senderAccountId, user.getRequest());
        accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse beforeDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RequestSpecs.MAX_DEPOSIT)
                .build();

        TestHelpers.repeat(2, () -> UserSteps.makeDeposit(user.getRequest(), depositRequest));

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance() * 2);
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        TransferResponse transferResponse = UserSteps.makeTransfer(user.getRequest(), transferRequest);

        ModelAssertions.assertThatModels(transferRequest, transferResponse).match();
        softly.assertThat(transferResponse.getMessage()).isEqualTo(ResponseSpecs.TRANSFER_SUCCESSFUL);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isCloseTo(depositRequest.getBalance() * 2 - transferAmount, within(0.01));
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isCloseTo(transferAmount, within(0.01));
    }

    @APIVersion("with_database")
    @DisplayName("User can transfer valid amount")
    @ParameterizedTest(name = "User can transfer valid amount: {0}")
    @ValueSource(doubles = {10000.0, 0.01, 1.0, 0.02, 9999, 9999.99})
    public void userCanTransferValidAmountsBetweenAccountsWithDB(double transferAmount) {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long senderAccountId = createAccountResponse.getId();
        CreateAccountResponse createAccountResponseSecond = UserSteps.createAccount(user.getRequest());
        long receiverAccountId = createAccountResponseSecond.getId();
        //accountsToDelete.put(senderAccountId, user.getRequest());
        //accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositSenderAccount, accountDao).match();

        GetUserAccountsResponse beforeDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDaoSecond = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositReceiverAccount, accountDaoSecond).match();

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RequestSpecs.MAX_DEPOSIT)
                .build();

        TestHelpers.repeat(2, () -> UserSteps.makeDeposit(user.getRequest(), depositRequest));

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDaoAfterDepositSenderAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        AccountDao accountDaoAfterDepositReceiverAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());

        DaoAndModelAssertions.assertThat(afterDepositSenderAccount, accountDaoAfterDepositSenderAccount).match();
        DaoAndModelAssertions.assertThat(afterDepositReceiverAccount, accountDaoAfterDepositReceiverAccount).match();

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance() * 2);
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        TransferResponse transferResponse = UserSteps.makeTransfer(user.getRequest(), transferRequest);

        AccountDao accountDaoAfterTransfer = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(transferResponse, accountDaoAfterTransfer).match();
        ModelAssertions.assertThatModels(transferRequest, transferResponse).match();

        AccountDao accountDaoAfterTransferSender = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        softly.assertThat(accountDaoAfterTransferSender.getBalance())
                .isCloseTo(depositRequest.getBalance() * 2 - transferRequest.getAmount(), within(0.01));
        softly.assertThat(transferResponse.getMessage()).isEqualTo(ResponseSpecs.TRANSFER_SUCCESSFUL);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        double accBalanceDB = DataBaseSteps.getBalanceByAccountNumber(accountDao.getAccountNumber());
        double accBalanceSecondDB = DataBaseSteps.getBalanceByAccountNumber(accountDaoSecond.getAccountNumber());

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isCloseTo(depositRequest.getBalance() * 2 - transferAmount, within(0.01));
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isCloseTo(transferAmount, within(0.01));

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(accBalanceDB);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(accBalanceSecondDB);
    }

    private static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of(10000.01, ResponseSpecs.TRANSFER_TOO_LARGE),
                Arguments.of(10001.00, ResponseSpecs.TRANSFER_TOO_LARGE),
                Arguments.of(-100.0, ResponseSpecs.INSUFFICIENT_FUNDS_INVALID_ACC),
                Arguments.of(0.0, ResponseSpecs.INSUFFICIENT_FUNDS_INVALID_ACC)
        );
    }

    @APIVersion("with_validation_fix")
    @DisplayName("User can't transfer invalid amount")
    @ParameterizedTest(name = "User can't transfer invalid amount: {0}")
    @MethodSource("invalidTransferAmounts")
    public void userCannotTransferInvalidAmount(double transferAmount, String expectedErrorMessage) {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        long senderAccountId = UserSteps.createAccount(user.getRequest()).getId();
        long receiverAccountId = UserSteps.createAccount(user.getRequest()).getId();
        accountsToDelete.put(senderAccountId, user.getRequest());
        accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse beforeDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RequestSpecs.MAX_DEPOSIT)
                .build();

        TestHelpers.repeat(3, () -> UserSteps.makeDeposit(user.getRequest(), depositRequest));

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance() * 3);
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        ErrorResponse errorResponse = UserSteps.attemptTransferAndGetBadRequest(user.getRequest(), transferRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(expectedErrorMessage);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance() * 3);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @APIVersion("with_database")
    @DisplayName("User can't transfer invalid amount")
    @ParameterizedTest(name = "User can't transfer invalid amount: {0}")
    @MethodSource("invalidTransferAmounts")
    public void userCannotTransferInvalidAmountWithDB(double transferAmount, String expectedErrorMessage) {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long senderAccountId = createAccountResponse.getId();
        CreateAccountResponse createAccountResponseSecond = UserSteps.createAccount(user.getRequest());
        long receiverAccountId = createAccountResponseSecond.getId();
        //accountsToDelete.put(senderAccountId, user.getRequest());
        //accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositSenderAccount, accountDao).match();

        GetUserAccountsResponse beforeDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDaoSecond = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositReceiverAccount, accountDaoSecond).match();

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RequestSpecs.MAX_DEPOSIT)
                .build();

        TestHelpers.repeat(3, () -> UserSteps.makeDeposit(user.getRequest(), depositRequest));

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDaoAfterDepositSenderAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        AccountDao accountDaoAfterDepositReceiverAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());

        DaoAndModelAssertions.assertThat(afterDepositSenderAccount, accountDaoAfterDepositSenderAccount).match();
        DaoAndModelAssertions.assertThat(afterDepositReceiverAccount, accountDaoAfterDepositReceiverAccount).match();

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance() * 3);
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        ErrorResponse errorResponse = UserSteps.attemptTransferAndGetBadRequest(user.getRequest(), transferRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(expectedErrorMessage);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        double accBalanceDB = DataBaseSteps.getBalanceByAccountNumber(accountDao.getAccountNumber());
        double accBalanceSecondDB = DataBaseSteps.getBalanceByAccountNumber(accountDaoSecond.getAccountNumber());

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance() * 3);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(accBalanceDB);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(accBalanceSecondDB);
    }

    @APIVersion("with_validation_fix")
    @DisplayName("User cannot transfer amount exceeding balance")
    @Test
    public void userCannotTransferAboveBalance() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        long senderAccountId = UserSteps.createAccount(user.getRequest()).getId();
        long receiverAccountId = UserSteps.createAccount(user.getRequest()).getId();
        accountsToDelete.put(senderAccountId, user.getRequest());
        accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse beforeDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(RandomData.getRandomTransferMoreThanDeposit(depositRequest.getBalance()))
                .build();

        ErrorResponse errorResponse = UserSteps.attemptTransferAndGetBadRequest(user.getRequest(), transferRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(ResponseSpecs.INSUFFICIENT_FUNDS_INVALID_ACC);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @APIVersion("with_database")
    @DisplayName("User cannot transfer amount exceeding balance")
    @Test
    public void userCannotTransferAboveBalanceWithDB() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long senderAccountId = createAccountResponse.getId();
        CreateAccountResponse createAccountResponseSecond = UserSteps.createAccount(user.getRequest());
        long receiverAccountId = createAccountResponseSecond.getId();
        //accountsToDelete.put(senderAccountId, user.getRequest());
        //accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositSenderAccount, accountDao).match();

        GetUserAccountsResponse beforeDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDaoSecond = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositReceiverAccount, accountDaoSecond).match();

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDaoAfterDepositSenderAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        AccountDao accountDaoAfterDepositReceiverAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());

        DaoAndModelAssertions.assertThat(afterDepositSenderAccount, accountDaoAfterDepositSenderAccount).match();
        DaoAndModelAssertions.assertThat(afterDepositReceiverAccount, accountDaoAfterDepositReceiverAccount).match();

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(RandomData.getRandomTransferMoreThanDeposit(depositRequest.getBalance()))
                .build();

        ErrorResponse errorResponse = UserSteps.attemptTransferAndGetBadRequest(user.getRequest(), transferRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(ResponseSpecs.INSUFFICIENT_FUNDS_INVALID_ACC);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        double accBalanceDB = DataBaseSteps.getBalanceByAccountNumber(accountDao.getAccountNumber());
        double accBalanceSecondDB = DataBaseSteps.getBalanceByAccountNumber(accountDaoSecond.getAccountNumber());

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(accBalanceDB);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(accBalanceSecondDB);
    }


    @APIVersion("with_validation_fix")
    @DisplayName("User cannot transfer amount from someone else's account")
    @Test
    public void userCannotTransferFromSomeoneElsesAccount() {
        User user = AdminSteps.createUser();
        User secondUser = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());
        usersToDelete.add(secondUser.getResponse().getId());

        long userAccountId = UserSteps.createAccount(user.getRequest()).getId();
        long secondUserAccountId = UserSteps.createAccount(secondUser.getRequest()).getId();
        accountsToDelete.put(userAccountId, user.getRequest());
        accountsToDelete.put(secondUserAccountId, secondUser.getRequest());

        GetUserAccountsResponse beforeTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), userAccountId);
        GetUserAccountsResponse beforeTransferReceiverAccount = UserSteps.getAccountById(secondUser.getRequest(), secondUserAccountId);

        softly.assertThat(beforeTransferSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(secondUserAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(secondUser.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(secondUser.getRequest(), secondUserAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), userAccountId);

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(secondUserAccountId)
                .receiverAccountId(userAccountId)
                .amount(RandomData.getRandomValidTransferLessOrEqualDeposit(depositRequest.getBalance()))
                .build();

        ErrorResponse errorResponse = UserSteps.attemptTransferAndGetForbidden(user.getRequest(), transferRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(ResponseSpecs.FORBIDDEN);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(secondUser.getRequest(), secondUserAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), userAccountId);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @APIVersion("with_database")
    @DisplayName("User cannot transfer amount from someone else's account")
    @Test
    public void userCannotTransferFromSomeoneElsesAccountWithDB() {
        User user = AdminSteps.createUser();
        User secondUser = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());
        usersToDelete.add(secondUser.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long userAccountId = createAccountResponse.getId();
        CreateAccountResponse createAccountResponseSecond = UserSteps.createAccount(secondUser.getRequest());
        long secondUserAccountId = createAccountResponseSecond.getId();
        //accountsToDelete.put(userAccountId, user.getRequest());
        //accountsToDelete.put(secondUserAccountId, secondUser.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), userAccountId);
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositSenderAccount, accountDao).match();

        GetUserAccountsResponse beforeDepositReceiverAccount = UserSteps.getAccountById(secondUser.getRequest(), secondUserAccountId);
        AccountDao accountDaoSecond = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositReceiverAccount, accountDaoSecond).match();

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(secondUserAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(secondUser.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), userAccountId);
        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(secondUser.getRequest(), secondUserAccountId);
        AccountDao accountDaoAfterDepositSenderAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        AccountDao accountDaoAfterDepositReceiverAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());

        DaoAndModelAssertions.assertThat(afterDepositReceiverAccount, accountDaoAfterDepositSenderAccount).match();
        DaoAndModelAssertions.assertThat(afterDepositSenderAccount, accountDaoAfterDepositReceiverAccount).match();

        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(secondUserAccountId)
                .receiverAccountId(userAccountId)
                .amount(RandomData.getRandomValidTransferLessOrEqualDeposit(depositRequest.getBalance()))
                .build();

        ErrorResponse errorResponse = UserSteps.attemptTransferAndGetForbidden(user.getRequest(), transferRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(ResponseSpecs.FORBIDDEN);

        double accBalanceDB = DataBaseSteps.getBalanceByAccountNumber(accountDao.getAccountNumber());
        double accBalanceSecondDB = DataBaseSteps.getBalanceByAccountNumber(accountDaoSecond.getAccountNumber());

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(secondUser.getRequest(), secondUserAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), userAccountId);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(accBalanceSecondDB);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(accBalanceDB);
    }

    @APIVersion("with_validation_fix")
    @DisplayName("User cannot transfer to non-existing account")
    @Test
    public void userCannotTransferToNonExistingAccount() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        long senderAccountId = UserSteps.createAccount(user.getRequest()).getId();
        accountsToDelete.put(senderAccountId, user.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(RequestSpecs.NON_EXISTENT_ACCOUNT_ID)
                .amount(RandomData.getRandomValidTransferLessOrEqualDeposit(depositRequest.getBalance()))
                .build();

        ErrorResponse errorResponse = UserSteps.attemptTransferAndGetBadRequest(user.getRequest(), transferRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(ResponseSpecs.INSUFFICIENT_FUNDS_INVALID_ACC);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
    }

    @APIVersion("with_database")
    @DisplayName("User cannot transfer to non-existing account")
    @Test
    public void userCannotTransferToNonExistingAccountWithDB() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long senderAccountId = createAccountResponse.getId();
        //accountsToDelete.put(senderAccountId, user.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositSenderAccount, accountDao).match();

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        AccountDao accountDaoAfterDepositSenderAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(afterDepositSenderAccount, accountDaoAfterDepositSenderAccount).match();

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(RequestSpecs.NON_EXISTENT_ACCOUNT_ID)
                .amount(RandomData.getRandomValidTransferLessOrEqualDeposit(depositRequest.getBalance()))
                .build();

        ErrorResponse errorResponse = UserSteps.attemptTransferAndGetBadRequest(user.getRequest(), transferRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(ResponseSpecs.INSUFFICIENT_FUNDS_INVALID_ACC);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);

        double accBalanceDB = DataBaseSteps.getBalanceByAccountNumber(accountDao.getAccountNumber());

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(accBalanceDB);
    }

    @APIVersion("with_validation_fix")
    @DisplayName("User cannot transfer money from non-existing account")
    @Test
    public void userCannotTransferFromNonExistingAccount() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        long receiverAccountId = UserSteps.createAccount(user.getRequest()).getId();
        accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(RequestSpecs.NON_EXISTENT_ACCOUNT_ID)
                .receiverAccountId(receiverAccountId)
                .amount(RandomData.getRandomValidDepositAmount())
                .build();

        ErrorResponse errorResponse = UserSteps.attemptTransferAndGetForbidden(user.getRequest(), transferRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(ResponseSpecs.FORBIDDEN);

        GetUserAccountsResponse afterTransferAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterTransferAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @APIVersion("with_database")
    @DisplayName("User cannot transfer money from non-existing account")
    @Test
    public void userCannotTransferFromNonExistingAccountWithDB() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse createAccountResponseSecond = UserSteps.createAccount(user.getRequest());
        long receiverAccountId = createAccountResponseSecond.getId();
        //accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositSenderAccount, accountDao).match();

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(RequestSpecs.NON_EXISTENT_ACCOUNT_ID)
                .receiverAccountId(receiverAccountId)
                .amount(RandomData.getRandomValidDepositAmount())
                .build();

        ErrorResponse errorResponse = UserSteps.attemptTransferAndGetForbidden(user.getRequest(), transferRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(ResponseSpecs.FORBIDDEN);

        GetUserAccountsResponse afterTransferAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        double accBalanceDB = DataBaseSteps.getBalanceByAccountNumber(accountDao.getAccountNumber());

        softly.assertThat(afterTransferAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        softly.assertThat(afterTransferAccount.getBalance())
                .isEqualTo(accBalanceDB);
    }

    @APIVersion("with_validation_fix")
    @DisplayName("Unauthorized user cannot transfer")
    @Test
    public void unauthorizedUserCannotTransfer() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        long senderAccountId = UserSteps.createAccount(user.getRequest()).getId();
        long receiverAccountId = UserSteps.createAccount(user.getRequest()).getId();
        accountsToDelete.put(senderAccountId, user.getRequest());
        accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse beforeTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(beforeTransferSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(RandomData.getRandomValidTransferLessOrEqualDeposit(depositRequest.getBalance()))
                .build();

        UserSteps.attemptTransferUnauthorizedUser(transferRequest);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @APIVersion("with_database")
    @DisplayName("Unauthorized user cannot transfer")
    @Test
    public void unauthorizedUserCannotTransferWithDB() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long senderAccountId = createAccountResponse.getId();
        CreateAccountResponse createAccountResponseSecond = UserSteps.createAccount(user.getRequest());
        long receiverAccountId = createAccountResponseSecond.getId();
        //accountsToDelete.put(senderAccountId, user.getRequest());
        //accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositSenderAccount, accountDao).match();

        GetUserAccountsResponse beforeDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDaoSecond = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositReceiverAccount, accountDaoSecond).match();

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDaoAfterDepositSenderAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        AccountDao accountDaoAfterDepositReceiverAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());

        DaoAndModelAssertions.assertThat(afterDepositSenderAccount, accountDaoAfterDepositSenderAccount).match();
        DaoAndModelAssertions.assertThat(afterDepositReceiverAccount, accountDaoAfterDepositReceiverAccount).match();

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(RandomData.getRandomValidTransferLessOrEqualDeposit(depositRequest.getBalance()))
                .build();

        UserSteps.attemptTransferUnauthorizedUser(transferRequest);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        double accBalanceDB = DataBaseSteps.getBalanceByAccountNumber(accountDao.getAccountNumber());
        double accBalanceSecondDB = DataBaseSteps.getBalanceByAccountNumber(accountDaoSecond.getAccountNumber());

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(accBalanceDB);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(accBalanceSecondDB);
    }

    @APIVersion("with_validation_fix")
    @DisplayName("User with invalid auth cannot transfer")
    @Test
    public void userWithInvalidAuthCannotTransfer() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        long senderAccountId = UserSteps.createAccount(user.getRequest()).getId();
        long receiverAccountId = UserSteps.createAccount(user.getRequest()).getId();
        accountsToDelete.put(senderAccountId, user.getRequest());
        accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse beforeTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(beforeTransferSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(RandomData.getRandomValidTransferLessOrEqualDeposit(depositRequest.getBalance()))
                .build();

        UserSteps.attemptTransferWithBrokenToken(transferRequest);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @APIVersion("with_database")
    @DisplayName("User with invalid auth cannot transfer")
    @Test
    public void userWithInvalidAuthCannotTransferWithDB() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long senderAccountId = createAccountResponse.getId();
        CreateAccountResponse createAccountResponseSecond = UserSteps.createAccount(user.getRequest());
        long receiverAccountId = createAccountResponseSecond.getId();
        //accountsToDelete.put(senderAccountId, user.getRequest());
        //accountsToDelete.put(receiverAccountId, user.getRequest());

        GetUserAccountsResponse beforeDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositSenderAccount, accountDao).match();

        GetUserAccountsResponse beforeDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDaoSecond = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(beforeDepositReceiverAccount, accountDaoSecond).match();

        softly.assertThat(beforeDepositSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.makeDeposit(user.getRequest(), depositRequest);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);
        AccountDao accountDaoAfterDepositSenderAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        AccountDao accountDaoAfterDepositReceiverAccount = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());

        DaoAndModelAssertions.assertThat(afterDepositSenderAccount, accountDaoAfterDepositSenderAccount).match();
        DaoAndModelAssertions.assertThat(afterDepositReceiverAccount, accountDaoAfterDepositReceiverAccount).match();

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(RandomData.getRandomValidTransferLessOrEqualDeposit(depositRequest.getBalance()))
                .build();

        UserSteps.attemptTransferWithBrokenToken(transferRequest);

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        double accBalanceDB = DataBaseSteps.getBalanceByAccountNumber(accountDao.getAccountNumber());
        double accBalanceSecondDB = DataBaseSteps.getBalanceByAccountNumber(accountDaoSecond.getAccountNumber());

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(accBalanceDB);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(accBalanceSecondDB);
    }
}


