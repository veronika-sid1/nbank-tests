package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositResponse {
    private int id;
    private String accountNumber;
    private double balance;
    private List<Transactions> transactions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Transactions {
        private int id;
        private double amount;
        private String type;
        private String timestamp;
        private int relatedAccountId;
    }
}
