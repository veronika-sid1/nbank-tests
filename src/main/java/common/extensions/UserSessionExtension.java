package common.extensions;

import api.entities.User;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import groovyjarjarantlr4.runtime.tree.BaseTree;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;

import java.util.LinkedList;
import java.util.List;

@Order(1)
public class UserSessionExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        UserSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation != null) {
            int userCount = annotation.value();

            SessionStorage.clear();

            List<User> users = new LinkedList<>();

            for (int i = 0; i < userCount; i++) {
                User user = AdminSteps.createUser();
                users.add(user);
            }

            SessionStorage.addUsers(users);

            int authAsUser = annotation.auth();

            BasePage.authAsUser(SessionStorage.getUser(authAsUser).getRequest());
        }
    }
}
