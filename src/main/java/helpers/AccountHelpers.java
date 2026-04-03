package helpers;

import models.GetUserAccountsResponse;
import models.UpdateProfileResponse;

import java.util.List;

public class AccountHelpers {
    public static GetUserAccountsResponse getAccountById(List<GetUserAccountsResponse> accounts, int accId) {
        return accounts.stream()
                .filter(acc -> acc.getId() == accId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account is not found: " + accId));
    }
}
