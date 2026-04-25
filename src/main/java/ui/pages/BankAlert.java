package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    SUCCESSFUL_DEPOSIT("✅ Successfully deposited"),
    EXCEEDING_MAX_DEPOSIT("❌ Please deposit less or equal to 5000$."),
    INVALID_AMOUNT("❌ Please enter a valid amount."),
    ACCOUNT_NOT_SELECTED("❌ Please select an account."),
    SUCCESSFUL_TRANSFER("✅ Successfully transferred"),
    ALL_FIELDS_MUST_BE_FILLED("❌ Please fill all fields and confirm."),
    CANNOT_TRANSFER_TO_SAME_ACCOUNT("❌ You cannot transfer money to the same account."),
    NO_USER_WITH_THIS_ACCOUNT("❌ No user found with this account number."),
    TRANSFER_MUST_BE_AT_LEAST_0_01("❌ Error: Transfer amount must be at least 0.01"),
    TRANSFER_CANNOT_EXCEED_MAX_LIMIT("❌ Error: Transfer amount cannot exceed 10000"),
    INCORRECT_RECIPIENT_NAME("❌ The recipient name does not match the registered name."),
    INSUFFICIENT_FUNDS_INVALID_ACC("Invalid transfer: insufficient funds or invalid accounts"),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS("Name must contain two words with letters only"),
    ENTER_VALID_NAME("Please enter a valid name"),
    NEW_NAME_SAME_AS_CURRENT("New name is the same as the current one.");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
