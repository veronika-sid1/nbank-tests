package iteration2.api;

import generators.RandomData;
import helpers.AccountHelpers;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.GetUserAccountsRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class DepositTest extends BaseTest {

    //positive: user can deposit min 0.01
    //positive: user can deposit 4999
    //positive: user can deposit 4999.99
    //positive: user can deposit max 5000

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 4999, 4999.99, 5000})
    public void userCanDepositValidAmounts(double amount) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int accId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accId)
                .balance(amount)
                .build();

        DepositResponse depositResponse = new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest).extract().as(DepositResponse.class);

        softly.assertThat(depositResponse.getTransactions().getFirst().getAmount())
                .isEqualTo(depositRequest.getBalance());
        softly.assertThat(depositResponse.getTransactions().getFirst().getType())
                .isEqualTo(TransactionType.DEPOSIT.name());
        softly.assertThat(depositResponse.getBalance())
                .isEqualTo(depositRequest.getBalance());

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse account = AccountHelpers.getAccountById(userAccountsResponse, accId);

        softly.assertThat(account.getBalance())
                .isEqualTo(depositRequest.getBalance());
    }

    //negative: user cannot deposit 5000.01
    //negative: user cannot deposit 5001
    //negative: user cannot deposit zero amount
    //negative: user cannot deposit negative amount

    private static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of(5000.01, "Deposit amount cannot exceed 5000"),
                Arguments.of(5001, "Deposit amount cannot exceed 5000"),
                Arguments.of(0, "Deposit amount must be at least 0.01"),
                Arguments.of(-100, "Deposit amount must be at least 0.01")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDepositAmounts")
    public void userCannotDepositInvalidAmount(double amount, String expectedErrorMessage) {
        double initialBalance = 0.0;

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int accId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accId)
                .balance(amount)
                .build();

        String actualErrorMessage = new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest())
                .post(depositRequest).extract().asString();

        softly.assertThat(actualErrorMessage)
                        .isEqualTo(expectedErrorMessage);

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse account = AccountHelpers.getAccountById(userAccountsResponse, accId);

        softly.assertThat(account.getBalance())
                .isEqualTo(initialBalance);
    }

    // negative: user cannot deposit to non-existent account
    @Test
    public void userCannotDepositToNonExistentAccount() {
        int nonExistentAccountId = Integer.MAX_VALUE;
        double depositAmount = 1.0;
        String expectedErrorMessage = "Unauthorized access to account";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(nonExistentAccountId)
                .balance(depositAmount)
                .build();

        String actualErrorMessage = new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden())
                .post(depositRequest).extract().asString();

        softly.assertThat(actualErrorMessage)
                .isEqualTo(expectedErrorMessage);
    }

    // negative: user cannot deposit to someone else's account
    @Test
    public void userCannotDepositToAnotherUsersAccount() {
        double initialBalance = 0.0;
        double depositAmount = 1.0;
        String expectedErrorMessage = "Unauthorized access to account";

        // создаём пользователя 1
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // создаём пользователя 2
        CreateUserRequest userRequest2 = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest2);

        // создаём акк первому пользователю
        int accId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        // создаём акк второму пользователю
        int accId2 = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest2.getUsername(), userRequest2.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        //проверяем, что в ответе вернулась ошибка при попытке депозита на чужой акк
        DepositRequest depositRequest = DepositRequest.builder()
                .id(accId2)
                .balance(depositAmount)
                .build();

        String actualErrorMessage = new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden())
                .post(depositRequest).extract().asString();

        softly.assertThat(actualErrorMessage)
                .isEqualTo(expectedErrorMessage);

        //проверяем, что балансы аккаунтов 1 и 2 не изменились
        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        List<GetUserAccountsResponse> userAccountsResponse2 = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest2.getUsername(), userRequest2.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse account = AccountHelpers.getAccountById(userAccountsResponse, accId);
        GetUserAccountsResponse account2 = AccountHelpers.getAccountById(userAccountsResponse2, accId2);

        softly.assertThat(account.getBalance())
                .isEqualTo(initialBalance);
        softly.assertThat(account2.getBalance())
                .isEqualTo(initialBalance);
    }

    //negative: unauthorized user cannot deposit
    @Test
    public void unauthorizedUserCannotDeposit() {
        double initialBalance = 0.0;
        double depositAmount = 5.0;

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int accId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accId)
                .balance(depositAmount)
                .build();

        new DepositRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsUnauthorized())
                .post(depositRequest);

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse account = AccountHelpers.getAccountById(userAccountsResponse, accId);

        softly.assertThat(account.getBalance())
                .isEqualTo(initialBalance);
    }

    //negative: user with invalid auth cannot deposit
    @Test
    public void userWithInvalidAuthCannotDeposit() {
        double initialBalance = 0.0;
        double depositAmount = 5.0;

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int accId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accId)
                .balance(depositAmount)
                .build();

        new DepositRequester(
                RequestSpecs.brokenAuthUserSpec(),
                ResponseSpecs.requestReturnsUnauthorized())
                .post(depositRequest);

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse account = AccountHelpers.getAccountById(userAccountsResponse, accId);

        softly.assertThat(account.getBalance())
                .isEqualTo(initialBalance);
    }
}
