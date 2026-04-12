package iteration2.api;

import entities.User;
import generators.RandomData;
import helpers.TestHelpers;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferTest extends BaseTest {
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

    private static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of(10000.01, ResponseSpecs.TRANSFER_TOO_LARGE),
                Arguments.of(10001.00, ResponseSpecs.TRANSFER_TOO_LARGE),
                Arguments.of(-100.0, ResponseSpecs.TRANSFER_TOO_SMALL),
                Arguments.of(0.0, ResponseSpecs.TRANSFER_TOO_SMALL)
        );
    }

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
}


