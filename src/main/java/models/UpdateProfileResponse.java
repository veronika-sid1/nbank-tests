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
public class UpdateProfileResponse {
    private Customer customer;
    private String message;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Customer {
        private int id;
        private String username;
        private String password;
        private String name;
        private String role;
        private List<Account> accounts;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Account {
        private int id;
        private String accountNumber;
        private double balance;
        private List<Transaction> transactions;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Transaction {
        private int id;
        private double amount;
        private String type;
        private String timestamp;
        private int relatedAccountId;
    }
}