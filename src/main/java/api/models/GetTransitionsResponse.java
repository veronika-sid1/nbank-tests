package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetTransitionsResponse extends BaseModel {
    private long id;
    private double amount;
    private String type;
    private String timestamp;
    private String status;
    private boolean fraudCheckRequired;
    private String timestampAsString;
    private long relatedAccountId;
    private double amountAsDouble;
    private RelatedAccount relatedAccount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RelatedAccount {
        private long id;
        private String accountNumber;
        private double balance;
    }
}
