package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositResponse extends BaseModel {
    private long id;
    private String accountNumber;
    private double balance;
    private List<Transactions> transactions;
    private String timestampAsString;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Transactions {
        private long id;
        private double amount;
        private String type;
        private String timestamp;
        private String timestampAsString;
        private long relatedAccountId;
        private double amountAsDouble;
    }
}
