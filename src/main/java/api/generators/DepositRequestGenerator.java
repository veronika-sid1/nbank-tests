package api.generators;

import api.models.DepositRequest;

public class DepositRequestGenerator extends DepositRequest {
    public static DepositRequest withAmount(long accountId, double amount) {
        return DepositRequest.builder()
                .accountId(accountId)
                .amount(amount)
                .build();
    }
}