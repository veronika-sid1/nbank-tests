package common.extensions;

import api.entities.User;
import api.models.CreateAccountResponse;
import api.requests.steps.UserSteps;
import common.annotations.UserAccount;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;

@Order(2)
public class UserAccountExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        UserAccount annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserAccount.class);

        if (annotation != null) {
            int accountCount = annotation.value();

            List<User> users = SessionStorage.getUsers();

            for (User user : users) {
                List<CreateAccountResponse> accounts = new ArrayList<>();

                for (int i = 0; i < accountCount; i++) {
                    accounts.add(UserSteps.createAccount(user.getRequest()));
                }

                SessionStorage.addAccount(user, accounts);
            }
        }
    }
}
