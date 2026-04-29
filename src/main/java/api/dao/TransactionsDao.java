package api.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionsDao {
    private Long id;
    private double amount;
    private String type;
    private Long account_id;
    private Long related_account_id;
}
