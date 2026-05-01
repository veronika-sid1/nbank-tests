package api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

import java.util.List;

public class ResponseSpecs {
    public static final String NONAME = "Noname";

    public static final String PROFILE_UPDATED = "Profile updated successfully";
    public static final String DEPOSIT_TOO_LARGE = "Deposit amount exceeds the 5000 limit";
    public static final String DEPOSIT_TOO_SMALL = "Invalid account or amount";
    public static final String FORBIDDEN = "Unauthorized access to account";
    public static final String ACCOUNT_NOT_FOUND = "Account is not found: ";
    public static final String TRANSFER_SUCCESSFUL = "Transfer successful";
    public static final String TRANSFER_TOO_LARGE = "Transfer amount cannot exceed 10000";
    public static final String TRANSFER_TOO_SMALL = "Transfer amount must be at least 0.01";
    public static final String INSUFFICIENT_FUNDS_INVALID_ACC = "Invalid transfer: insufficient funds or invalid accounts";
    public static final String NAME_VALIDATION = "Name must contain two words with letters only";
    public static final String WITHOUT_CHECKING_FRAUD_MESSAGE = "This transaction does not require fraud checking.";
    public static final String WITHOUT_CHECKING_FRAUD_STATUS = "NO_FRAUD_CHECK_REQUIRED";

    private ResponseSpecs() {}

    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreated() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification requestReturnsOK() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequest() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .build();
    }

    public static ResponseSpecification requestReturnsForbidden() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .build();
    }

    public static ResponseSpecification requestReturnsUnauthorized() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .build();
    }
}
