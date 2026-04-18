package api.requests.steps;

import api.models.*;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import io.qameta.allure.Step;

import java.util.Arrays;
import java.util.List;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Step("Create account for user {userRequest.username}")
    public static CreateAccountResponse createAccount(CreateUserRequest userRequest) {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post();
    }

    @Step("Deposit amount {depositRequest.balance} to account {depositRequest.id}")
    public static DepositResponse makeDeposit(CreateUserRequest user, DepositRequest depositRequest) {
        return new ValidatedCrudRequester<DepositResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);
    }

    @Step("Get account by id {accId} for user {user.username}")
    public static GetUserAccountsResponse getAccountById(CreateUserRequest user, long accId) {
        List<GetUserAccountsResponse> userAccountsResponse = Arrays.asList(new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetUserAccountsResponse[].class));

        return userAccountsResponse.stream()
                .filter(acc -> acc.getId() == accId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ResponseSpecs.ACCOUNT_NOT_FOUND + accId));
    }

    @Step("Transfer amount {transferRequest.amount} to account {transferRequest.receiverAccountId}")
    public static TransferResponse makeTransfer(CreateUserRequest user, TransferRequest transferRequest) {
        return new ValidatedCrudRequester<TransferResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest);
    }

    @Step("Update name")
    public static UpdateProfileResponse updateUserName(CreateUserRequest user, UpdateProfileRequest updateProfileRequest) {
        return new ValidatedCrudRequester<UpdateProfileResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .update(updateProfileRequest);
    }

    @Step("Attempt to deposit without authorization")
    public static void attemptDepositUnauthorized(DepositRequest depositRequest) {
        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsUnauthorized())
                .post(depositRequest);
    }

    @Step("Attempt to deposit with broken auth")
    public static void attemptDepositWithBrokenAuth(DepositRequest depositRequest) {
        new CrudRequester(
                RequestSpecs.brokenAuthUserSpec(),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsUnauthorized())
                .post(depositRequest);
    }

    @Step("Attempt to update name using invalid data")
    public static ErrorResponse attemptUpdateUsernameUsingInvalidData(CreateUserRequest user, UpdateProfileRequest updateProfileRequest) {
        String errorResponse = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsBadRequest())
                .update(updateProfileRequest).extract().asString();

        return new ErrorResponse(errorResponse);
    }

    @Step("Attempt to update name without authorization")
    public static void updateUserNameUnauthorized(UpdateProfileRequest updateProfileRequest) {
        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsUnauthorized())
                .update(updateProfileRequest).extract().asString();
    }

    @Step("Attempt to update name with broken auth")
    public static void updateUserNameWithBrokenAuth(UpdateProfileRequest updateProfileRequest) {
        new CrudRequester(
                RequestSpecs.brokenAuthUserSpec(),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsUnauthorized())
                .update(updateProfileRequest).extract().asString();
    }

    @Step("Get name")
    public static GetProfileResponse getProfile(CreateUserRequest user) {
        return new ValidatedCrudRequester<GetProfileResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.GET_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();
    }

    @Step("Attempt to transfer amount {transferRequest.amount} to account {transferRequest.receiverAccountId} to get Bad Request error")
    public static ErrorResponse attemptTransferAndGetBadRequest(CreateUserRequest user, TransferRequest transferRequest) {
        String errorResponse = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest())
                .post(transferRequest).extract().asString();

        return new ErrorResponse(errorResponse);
    }

    @Step("Attempt to transfer amount {transferRequest.amount} from account {transferRequest.senderAccountId} to get Forbidden error")
    public static ErrorResponse attemptTransferAndGetForbidden(CreateUserRequest user, TransferRequest transferRequest) {
        String errorResponse = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsForbidden())
                .post(transferRequest).extract().asString();

        return new ErrorResponse(errorResponse);
    }

    @Step("Attempt to transfer amount {transferRequest.amount} while being unauthorized")
    public static ErrorResponse attemptTransferUnauthorizedUser(TransferRequest transferRequest) {
        String errorResponse = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsUnauthorized())
                .post(transferRequest).extract().asString();

        return new ErrorResponse(errorResponse);
    }

    @Step("Attempt to transfer amount {transferRequest.amount} with broken token")
    public static ErrorResponse attemptTransferWithBrokenToken(TransferRequest transferRequest) {
        String errorResponse = new CrudRequester(
                RequestSpecs.brokenAuthUserSpec(),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsUnauthorized())
                .post(transferRequest).extract().asString();

        return new ErrorResponse(errorResponse);
    }

    @Step("Attempt to deposit invalid amount {depositRequest.balance} to account {depositRequest.id} to get Bad Request error")
    public static ErrorResponse attemptDepositAndGetBadRequest(CreateUserRequest user, DepositRequest depositRequest) {
        String errorResponse = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsBadRequest())
                .post(depositRequest).extract().asString();

        return new ErrorResponse(errorResponse);
    }

    @Step("Attempt to deposit {depositRequest.balance} to account {depositRequest.id} to get Forbidden error")
    public static ErrorResponse attemptDepositAndGetForbidden(CreateUserRequest user, DepositRequest depositRequest) {
        String errorResponse = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsForbidden())
                .post(depositRequest).extract().asString();

        return new ErrorResponse(errorResponse);
    }

    @Step("Delete account")
    public static void deleteAccount(long accountId, CreateUserRequest user){
        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.DELETE_ACCOUNT,
                ResponseSpecs.requestReturnsOK())
                .delete(accountId);
    }

    public List<GetUserAccountsResponse> getAllAccounts() {
        return new ValidatedCrudRequester<GetUserAccountsResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .getAll(GetUserAccountsResponse[].class);
    }
}
