package base;

import api.models.CreateUserRequest;
import common.extensions.TimingExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(TimingExtension.class)
public class BaseTest {
    protected SoftAssertions softly;
    protected Map<Long, CreateUserRequest> accountsToDelete = new HashMap<>();
    protected List<Long> usersToDelete = new ArrayList<>();

    @BeforeEach
    public void beforeTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        try {
            softly.assertAll();
        } finally {
            deleteEntities();
        }
    }

    public void deleteEntities() {
        for (Map.Entry<Long, CreateUserRequest> acc : accountsToDelete.entrySet()) {
            try {
                UserSteps.deleteAccount(acc.getKey(), acc.getValue());
                System.out.println("Deleted acc: " + acc.getKey());
            } catch (Exception e) {
                System.out.println("Failed to delete acc: " + acc.getKey());
            }
        }
        for (long user : usersToDelete) {
            try {
                AdminSteps.deleteUser(user);
                System.out.println("Deleted user: " + user);
            } catch (Exception e) {
                System.out.println("Failed to delete user: " + user);
            }
        }

        accountsToDelete.clear();
        usersToDelete.clear();
    }
}
