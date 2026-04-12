package iteration2.api;

import entities.User;
import generators.RandomData;
import models.*;
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

public class DepositTest extends BaseTest {
    @DisplayName("User can deposit valid amount")
    @ParameterizedTest(name = "User can deposit valid amount: {0}")
    @ValueSource(doubles = {0.01, 4999, 4999.99, 5000})
    public void userCanDepositValidAmounts(double amount) {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        long accId = UserSteps.createAccount(user.getRequest()).getId();
        accountsToDelete.put(accId, user.getRequest());

        GetUserAccountsResponse beforeDeposit = UserSteps.getAccountById(user.getRequest(), accId);

        softly.assertThat(beforeDeposit.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accId)
                .balance(amount)
                .build();

        DepositResponse depositResponse = UserSteps.makeDeposit(user.getRequest(), depositRequest);

        softly.assertThat(depositResponse.getTransactions().getFirst().getType())
                .isEqualTo(TransactionType.DEPOSIT.name());

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), accId);

        softly.assertThat(account.getBalance())
                .isEqualTo(depositRequest.getBalance());
    }

    private static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of(5000.01, ResponseSpecs.DEPOSIT_TOO_LARGE),
                Arguments.of(5001, ResponseSpecs.DEPOSIT_TOO_LARGE),
                Arguments.of(0, ResponseSpecs.DEPOSIT_TOO_SMALL),
                Arguments.of(-100, ResponseSpecs.DEPOSIT_TOO_SMALL)
        );
    }

    @DisplayName("User cannot deposit invalid amount: {0}")
    @ParameterizedTest(name = "User cannot deposit invalid amount: {0}")
    @MethodSource("invalidDepositAmounts")
    public void userCannotDepositInvalidAmount(double amount, String expectedErrorMessage) {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        long accId = UserSteps.createAccount(user.getRequest()).getId();
        accountsToDelete.put(accId, user.getRequest());

        GetUserAccountsResponse beforeDeposit = UserSteps.getAccountById(user.getRequest(), accId);

        softly.assertThat(beforeDeposit.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accId)
                .balance(amount)
                .build();

        ErrorResponse errorResponse = UserSteps.attemptDepositAndGetBadRequest(user.getRequest(), depositRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(expectedErrorMessage);

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), accId);

        softly.assertThat(account.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User cannot deposit to non-existent account")
    @Test
    public void userCannotDepositToNonExistentAccount() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        DepositRequest depositRequest = DepositRequest.builder()
                .id(RequestSpecs.NON_EXISTENT_ACCOUNT_ID)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        ErrorResponse errorResponse = UserSteps.attemptDepositAndGetForbidden(user.getRequest(), depositRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(ResponseSpecs.FORBIDDEN);
    }

    @DisplayName("User cannot deposit to someone else's account")
    @Test
    public void userCannotDepositToAnotherUsersAccount() {
        User user = AdminSteps.createUser();
        User secondUser = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());
        usersToDelete.add(secondUser.getResponse().getId());

        long accId = UserSteps.createAccount(user.getRequest()).getId();
        long secondUserAccId = UserSteps.createAccount(secondUser.getRequest()).getId();
        accountsToDelete.put(accId, user.getRequest());
        accountsToDelete.put(secondUserAccId, secondUser.getRequest());

        GetUserAccountsResponse beforeDeposit = UserSteps.getAccountById(user.getRequest(), accId);

        softly.assertThat(beforeDeposit.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        GetUserAccountsResponse beforeDepositSecondUser = UserSteps.getAccountById(secondUser.getRequest(), secondUserAccId);

        softly.assertThat(beforeDepositSecondUser.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(secondUserAccId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        ErrorResponse errorResponse = UserSteps.attemptDepositAndGetForbidden(user.getRequest(), depositRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(ResponseSpecs.FORBIDDEN);

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), accId);
        GetUserAccountsResponse secondUserAccount = UserSteps.getAccountById(secondUser.getRequest(), secondUserAccId);

        softly.assertThat(account.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(secondUserAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("Unauthorized user cannot deposit")
    @Test
    public void unauthorizedUserCannotDeposit() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        long accId = UserSteps.createAccount(user.getRequest()).getId();
        accountsToDelete.put(accId, user.getRequest());

        GetUserAccountsResponse beforeDeposit = UserSteps.getAccountById(user.getRequest(), accId);

        softly.assertThat(beforeDeposit.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.attemptDepositUnauthorized(depositRequest);

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), accId);

        softly.assertThat(account.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @DisplayName("User with invalid auth cannot deposit")
    @Test
    public void userWithInvalidAuthCannotDeposit() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        long accId = UserSteps.createAccount(user.getRequest()).getId();
        accountsToDelete.put(accId, user.getRequest());

        GetUserAccountsResponse beforeDeposit = UserSteps.getAccountById(user.getRequest(), accId);

        softly.assertThat(beforeDeposit.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        UserSteps.attemptDepositWithBrokenAuth(depositRequest);

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), accId);

        softly.assertThat(account.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }
}
