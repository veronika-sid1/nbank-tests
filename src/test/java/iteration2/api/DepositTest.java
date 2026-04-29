package iteration2.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.entities.User;
import api.generators.RandomData;
import api.models.*;
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

public class DepositTest extends BaseTest {
    @APIVersion("with_validation_fix")
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

    @APIVersion("with_database")
    @DisplayName("User can deposit valid amount")
    @ParameterizedTest(name = "User can deposit valid amount: {0}")
    @ValueSource(doubles = {0.01, 4999, 4999.99, 5000})
    public void userCanDepositValidAmountsWithDB(double amount) {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long accId = createAccountResponse.getId();
        //accountsToDelete.put(accId, user.getRequest());

        GetUserAccountsResponse beforeDeposit = UserSteps.getAccountById(user.getRequest(), accId);
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());

        DaoAndModelAssertions.assertThat(beforeDeposit, accountDao).match();

        softly.assertThat(beforeDeposit.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accId)
                .balance(amount)
                .build();

        DepositResponse depositResponse = UserSteps.makeDeposit(user.getRequest(), depositRequest);
        AccountDao accountDaoAfterDeposit = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());

        DaoAndModelAssertions.assertThat(depositResponse, accountDaoAfterDeposit).match();

        softly.assertThat(depositResponse.getTransactions().getFirst().getType())
                .isEqualTo(TransactionType.DEPOSIT.name());

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), accId);

        softly.assertThat(account.getBalance())
                .isEqualTo(depositRequest.getBalance());

        //тк нет ручки удаления в этой версии, удалим из бд, если нет зависимостей по FK
        DataBaseSteps.deleteAccount(account.getAccountNumber());
    }

    @APIVersion("with_validation_fix")
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

    @APIVersion("with_database")
    @DisplayName("User cannot deposit invalid amount: {0}")
    @ParameterizedTest(name = "User cannot deposit invalid amount: {0}")
    @MethodSource("invalidDepositAmounts")
    public void userCannotDepositInvalidAmountWithDB(double amount, String expectedErrorMessage) {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long accId = createAccountResponse.getId();
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());

        GetUserAccountsResponse beforeDeposit = UserSteps.getAccountById(user.getRequest(), accId);
        DaoAndModelAssertions.assertThat(beforeDeposit, accountDao).match();

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

        AccountDao accountDaoAfterAttempt = DataBaseSteps.getAccountByAccountNumber(account.getAccountNumber());

        softly.assertThat(accountDaoAfterAttempt.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @APIVersion("with_validation_fix")
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

    @APIVersion("with_validation_fix")
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

    @APIVersion("with_database")
    @DisplayName("User cannot deposit to someone else's account")
    @Test
    public void userCannotDepositToAnotherUsersAccountWithDB() {
        User user = AdminSteps.createUser();
        User secondUser = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());
        usersToDelete.add(secondUser.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long accId = createAccountResponse.getId();
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());

        CreateAccountResponse createAccountResponseSecond = UserSteps.createAccount(secondUser.getRequest());
        long secondUserAccId = createAccountResponseSecond.getId();
        AccountDao accountDaoSecond = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());

        //accountsToDelete.put(accId, user.getRequest());
        //accountsToDelete.put(secondUserAccId, secondUser.getRequest());

        GetUserAccountsResponse beforeDeposit = UserSteps.getAccountById(user.getRequest(), accId);
        DaoAndModelAssertions.assertThat(beforeDeposit, accountDao).match();

        softly.assertThat(beforeDeposit.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(accountDao.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        GetUserAccountsResponse beforeDepositSecondUser = UserSteps.getAccountById(secondUser.getRequest(), secondUserAccId);
        DaoAndModelAssertions.assertThat(beforeDepositSecondUser, accountDaoSecond).match();

        softly.assertThat(beforeDepositSecondUser.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(accountDaoSecond.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(secondUserAccId)
                .balance(RandomData.getRandomValidDepositAmount())
                .build();

        ErrorResponse errorResponse = UserSteps.attemptDepositAndGetForbidden(user.getRequest(), depositRequest);

        softly.assertThat(errorResponse.getMessage())
                .isEqualTo(ResponseSpecs.FORBIDDEN);

        GetUserAccountsResponse account = UserSteps.getAccountById(user.getRequest(), accId);
        AccountDao accountDaoAfterAttempt = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());

        GetUserAccountsResponse secondUserAccount = UserSteps.getAccountById(secondUser.getRequest(), secondUserAccId);
        AccountDao accountDaoSecondAfterAttempt = DataBaseSteps.getAccountByAccountNumber(createAccountResponseSecond.getAccountNumber());

        softly.assertThat(account.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(accountDaoAfterAttempt.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(secondUserAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(accountDaoSecondAfterAttempt.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @APIVersion("with_validation_fix")
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

    @APIVersion("with_database")
    @DisplayName("Unauthorized user cannot deposit")
    @Test
    public void unauthorizedUserCannotDepositWithDB() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long accId = createAccountResponse.getId();
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());

        //accountsToDelete.put(accId, user.getRequest());

        GetUserAccountsResponse beforeDeposit = UserSteps.getAccountById(user.getRequest(), accId);
        DaoAndModelAssertions.assertThat(beforeDeposit, accountDao).match();

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

        AccountDao accountDaoAfterAttempt = DataBaseSteps.getAccountByAccountNumber(account.getAccountNumber());

        softly.assertThat(accountDaoAfterAttempt.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }

    @APIVersion("with_validation_fix")
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

    @APIVersion("with_database")
    @DisplayName("User with invalid auth cannot deposit")
    @Test
    public void userWithInvalidAuthCannotDepositWithDB() {
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(user.getRequest());
        long accId = createAccountResponse.getId();
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());

        GetUserAccountsResponse beforeDeposit = UserSteps.getAccountById(user.getRequest(), accId);
        DaoAndModelAssertions.assertThat(beforeDeposit, accountDao).match();

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

        AccountDao accountDaoAfterAttempt = DataBaseSteps.getAccountByAccountNumber(account.getAccountNumber());

        softly.assertThat(accountDaoAfterAttempt.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
    }
}
