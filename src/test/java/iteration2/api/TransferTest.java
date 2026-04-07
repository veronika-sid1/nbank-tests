package iteration2.api;

import generators.RandomData;
import helpers.AccountHelpers;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferTest extends BaseTest {
    //positive: user can transfer money between his accounts
    @Test
    public void userCanTransferMoneyBetweenAccounts() {
        double amount = 1.0;
        double balance = 3000.0;
        String expectedSuccessMessage = "Transfer successful";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int senderAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        int receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(balance)
                .build();

        new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        TransferResponse transferResponse = new TransferRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest).extract().as(TransferResponse.class);

        softly.assertThat(transferResponse.getSenderAccountId())
                        .isEqualTo(senderAccountId);
        softly.assertThat(transferResponse.getReceiverAccountId())
                        .isEqualTo(receiverAccountId);
        softly.assertThat(transferResponse.getAmount())
                        .isEqualTo(amount);
        softly.assertThat(transferResponse.getMessage()).isEqualTo(expectedSuccessMessage);
        
        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse senderAccount = AccountHelpers.getAccountById(userAccountsResponse, senderAccountId);
        GetUserAccountsResponse receiverAccount = AccountHelpers.getAccountById(userAccountsResponse, receiverAccountId);

        softly.assertThat(senderAccount.getBalance())
                .isEqualTo(balance - amount);
        softly.assertThat(receiverAccount.getBalance())
                .isEqualTo(amount);
    }

    //positive: user can transfer money to someone else's account
    @Test
    public void userCanTransferToAnotherUsersAccount() {
        double transferAmount = 9999.0;
        double firstDepositAmount = 5000.0;
        double secondDepositAmount = 4999.0;
        String expectedSuccessMessage = "Transfer successful";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        CreateUserRequest userRequest2 = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest2);

        int senderAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        int receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest2.getUsername(), userRequest2.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(firstDepositAmount)
                .build();

        DepositRequest depositRequest2 = DepositRequest.builder()
                .id(senderAccountId)
                .balance(secondDepositAmount)
                .build();

        new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest2);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        TransferResponse transferResponse = new TransferRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest).extract().as(TransferResponse.class);

        softly.assertThat(transferResponse.getSenderAccountId())
                .isEqualTo(senderAccountId);
        softly.assertThat(transferResponse.getReceiverAccountId())
                .isEqualTo(receiverAccountId);
        softly.assertThat(transferResponse.getAmount())
                .isEqualTo(transferAmount);
        softly.assertThat(transferResponse.getMessage()).isEqualTo(expectedSuccessMessage);

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        List<GetUserAccountsResponse> userAccountsResponseReceiver = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest2.getUsername(), userRequest2.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse senderAccount = AccountHelpers.getAccountById(userAccountsResponse, senderAccountId);
        GetUserAccountsResponse receiverAccount = AccountHelpers.getAccountById(userAccountsResponseReceiver, receiverAccountId);

        softly.assertThat(senderAccount.getBalance())
                .isEqualTo(firstDepositAmount + secondDepositAmount - transferAmount);
        softly.assertThat(receiverAccount.getBalance())
                .isEqualTo(transferAmount);
}
    //positive: user can transfer max 10000
    //positive: user can transfer min 0.01
    //positive: user can transfer 9999.99
    @ParameterizedTest
    @ValueSource(doubles = {10000.0, 0.01, 9999.99})
    public void userCanTransferValidAmounts(double transferAmount) {
        double depositAmount = 5000;
        double depositSum = 10000;
        String expectedSuccessMessage = "Transfer successful";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int senderAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        int receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        TransferResponse transferResponse = new TransferRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest).extract().as(TransferResponse.class);

        softly.assertThat(transferResponse.getSenderAccountId())
                .isEqualTo(senderAccountId);
        softly.assertThat(transferResponse.getReceiverAccountId())
                .isEqualTo(receiverAccountId);
        softly.assertThat(transferResponse.getAmount())
                .isEqualTo(transferAmount);
        softly.assertThat(transferResponse.getMessage()).isEqualTo(expectedSuccessMessage);

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse senderAccount = AccountHelpers.getAccountById(userAccountsResponse, senderAccountId);
        GetUserAccountsResponse receiverAccount = AccountHelpers.getAccountById(userAccountsResponse, receiverAccountId);

        softly.assertThat(senderAccount.getBalance())
                .isCloseTo(depositSum - transferAmount, within(0.000001));
        softly.assertThat(receiverAccount.getBalance())
                .isCloseTo(transferAmount, within(0.000001));
    }

    //negative: user cannot transfer 10000.01
    //negative: user cannot transfer more than 10001
    //negative: user cannot transfer negative amount
    //negative: user cannot transfer zero amount

    private static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000"),
                Arguments.of(10001.00, "Transfer amount cannot exceed 10000"),
                Arguments.of(-100.0, "Transfer amount must be at least 0.01"),
                Arguments.of(0.0, "Transfer amount must be at least 0.01")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransferAmounts")
    public void userCannotTransferInvalidAmount(double transferAmount, String errorMessage) {
        double depositAmount = 5000.0;
        double depositSum = 15000;
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

        int senderAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        int receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        String transferResponse = new TransferRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest())
                .post(transferRequest).extract().asString();

        softly.assertThat(transferResponse)
                .isEqualTo(errorMessage);

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse senderAccount = AccountHelpers.getAccountById(userAccountsResponse, senderAccountId);
        GetUserAccountsResponse receiverAccount = AccountHelpers.getAccountById(userAccountsResponse, receiverAccountId);

        softly.assertThat(senderAccount.getBalance())
                .isEqualTo(depositSum);
        softly.assertThat(receiverAccount.getBalance())
                .isEqualTo(initialBalance);
    }

    // negative: user cannot transfer amount exceeding balance
    @Test
    public void userCannotTransferAboveBalance() {
        double balanceTransfer = 200.0;
        double depositAmount = 100.0;
        double initialBalance = 0.0;
        String errorMessage = "Invalid transfer: insufficient funds or invalid accounts";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int senderAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        int receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(balanceTransfer)
                .build();

        String transferResponse = new TransferRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest())
                .post(transferRequest).extract().asString();

        softly.assertThat(transferResponse)
                .isEqualTo(errorMessage);

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse senderAccount = AccountHelpers.getAccountById(userAccountsResponse, senderAccountId);
        GetUserAccountsResponse receiverAccount = AccountHelpers.getAccountById(userAccountsResponse, receiverAccountId);

        softly.assertThat(senderAccount.getBalance())
                .isEqualTo(depositAmount);
        softly.assertThat(receiverAccount.getBalance())
                .isEqualTo(initialBalance);
    }

    //negative: user cannot transfer amount from someone else's account
    @Test
    public void userCannotTransferFromSomeoneElsesAccount() {
        double transferAmount = 100.0;
        double depositAmount = 5000.0;
        double initialBalance = 0.0;
        String errorMessage = "Unauthorized access to account";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        CreateUserRequest userRequest2 = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest2);

        int user1Account = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        int user2Account = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest2.getUsername(), userRequest2.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        DepositRequest depositRequest = DepositRequest.builder()
                .id(user2Account)
                .balance(depositAmount)
                .build();

        new DepositRequester(
                RequestSpecs.authAsUser(userRequest2.getUsername(), userRequest2.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(user2Account)
                .receiverAccountId(user1Account)
                .amount(transferAmount)
                .build();

        String transferResponse = new TransferRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden())
                .post(transferRequest).extract().asString();

        softly.assertThat(transferResponse)
                .isEqualTo(errorMessage);

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        List<GetUserAccountsResponse> userAccountsResponseReceiver = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest2.getUsername(), userRequest2.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse senderAccount = AccountHelpers.getAccountById(userAccountsResponse, user1Account);
        GetUserAccountsResponse receiverAccount = AccountHelpers.getAccountById(userAccountsResponseReceiver, user2Account);

        softly.assertThat(senderAccount.getBalance())
                .isEqualTo(initialBalance);
        softly.assertThat(receiverAccount.getBalance())
                .isEqualTo(depositAmount);
    }

    // negative: user cannot transfer to non-existing account
    @Test
    public void userCannotTransferToNonExistingAccount() {
        int nonExistentAccountId = Integer.MAX_VALUE;
        double transferAmount = 100.0;
        double depositAmount = 5000.0;
        String errorMessage = "Invalid transfer: insufficient funds or invalid accounts";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int userAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        DepositRequest depositRequest = DepositRequest.builder()
                .id(userAccountId)
                .balance(depositAmount)
                .build();

        new DepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(userAccountId)
                .receiverAccountId(nonExistentAccountId)
                .amount(transferAmount)
                .build();

        String transferResponse = new TransferRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest())
                .post(transferRequest).extract().asString();

        softly.assertThat(transferResponse)
                .isEqualTo(errorMessage);

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse senderAccount = AccountHelpers.getAccountById(userAccountsResponse, userAccountId);

        softly.assertThat(senderAccount.getBalance())
                .isEqualTo(depositAmount);
    }

    //negative: user cannot transfer from non-existing account
    @Test
    public void userCannotTransferFromNonExistingAccount() {
        int nonExistentAccountId = Integer.MAX_VALUE;
        double transferAmount = 100.0;
        double initialBalance = 0.0;
        String errorMessage = "Unauthorized access to account";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int userAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(nonExistentAccountId)
                .receiverAccountId(userAccountId)
                .amount(transferAmount)
                .build();

        String transferResponse = new TransferRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden())
                .post(transferRequest).extract().asString();

        softly.assertThat(transferResponse)
                .isEqualTo(errorMessage);

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse senderAccount = AccountHelpers.getAccountById(userAccountsResponse, userAccountId);

        softly.assertThat(senderAccount.getBalance())
                .isEqualTo(initialBalance);
    }

    //negative: unauthorized user cannot transfer
    @Test
    public void unauthorizedUserCannotTransfer() {
        double amount = 1.0;
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

        int senderAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        int receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new TransferRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsUnauthorized())
                .post(transferRequest);

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse senderAccount = AccountHelpers.getAccountById(userAccountsResponse, senderAccountId);
        GetUserAccountsResponse receiverAccount = AccountHelpers.getAccountById(userAccountsResponse, receiverAccountId);

        softly.assertThat(senderAccount.getBalance())
                .isEqualTo(initialBalance);
        softly.assertThat(receiverAccount.getBalance())
                .isEqualTo(initialBalance);
    }

    //negative: user with invalid auth cannot transfer
    @Test
    public void userWithInvalidAuthCannotTransfer() {
        double amount = 1.0;
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

        int senderAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        int receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().jsonPath().getInt("id");

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new TransferRequester(
                RequestSpecs.brokenAuthUserSpec(),
                ResponseSpecs.requestReturnsUnauthorized())
                .post(transferRequest);

        List<GetUserAccountsResponse> userAccountsResponse = new GetUserAccountsRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountsResponse.class);

        GetUserAccountsResponse senderAccount = AccountHelpers.getAccountById(userAccountsResponse, senderAccountId);
        GetUserAccountsResponse receiverAccount = AccountHelpers.getAccountById(userAccountsResponse, receiverAccountId);

        softly.assertThat(senderAccount.getBalance())
                .isEqualTo(initialBalance);
        softly.assertThat(receiverAccount.getBalance())
                .isEqualTo(initialBalance);
    }
}


