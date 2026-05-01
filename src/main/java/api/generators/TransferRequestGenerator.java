package api.generators;

import api.models.TransferRequest;

public class TransferRequestGenerator extends TransferRequest {
    public static TransferRequest makeRequest(long senderAccountId, long receiverAccountId, double amount) {
        return TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();
    }
}
