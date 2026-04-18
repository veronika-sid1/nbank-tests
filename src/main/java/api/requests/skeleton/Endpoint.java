package api.requests.skeleton;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),

    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),

    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    DEPOSIT(
            "/accounts/deposit",
            DepositRequest.class,
            DepositResponse.class
    ),

    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            GetUserAccountsResponse.class
    ),

    TRANSFER(
            "/accounts/transfer",
            TransferRequest.class,
            TransferResponse.class
    ),

    DELETE_USER(
            "/admin/users/{id}",
            BaseModel.class,
            BaseModel.class
            ),

    DELETE_ACCOUNT(
            "/accounts/{id}",
            BaseModel.class,
            BaseModel.class
    ),

    PROFILE(
            "/customer/profile",
            UpdateProfileRequest.class,
            UpdateProfileResponse.class
    ),

    GET_PROFILE(
            "/customer/profile",
            BaseModel.class,
            GetProfileResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
