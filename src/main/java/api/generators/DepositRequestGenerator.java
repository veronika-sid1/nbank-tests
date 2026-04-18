package api.generators;

import api.models.DepositRequest;

public class DepositRequestGenerator extends DepositRequest {
    public static DepositRequest withAmount(Long accountId, double amount) {
        return DepositRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();
    }
}