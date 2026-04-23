package common.storage;

import api.entities.User;
import api.models.CreateAccountResponse;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    public static final SessionStorage INSTANCE = new SessionStorage();
    private final LinkedHashMap<User, UserSteps> userStepsMap = new LinkedHashMap<>();
    private final LinkedHashMap<User, List<CreateAccountResponse>> userAccounts = new LinkedHashMap<>();

    private SessionStorage() {}

    public static void addUsers(List<User> users) {
        for (User user : users) {
            INSTANCE.userStepsMap.put(user, new UserSteps(user.getRequest().getUsername(), user.getRequest().getPassword()));
        }
    }

    public static void addAccount(User user, List<CreateAccountResponse> accounts) {
            INSTANCE.userAccounts.put(user, accounts);
    }

    public static User getUser(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.keySet()).get(number - 1);
    }

    public static User getUser() {
        return getUser(1);
    }

    public static List<User> getUsers() {
        return new ArrayList<>(INSTANCE.userStepsMap.keySet());
    }

    public static UserSteps getSteps(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.values()).get(number - 1);
    }

    public static UserSteps getSteps() {
        return getSteps(1);
    }

    public static CreateAccountResponse getAccount(int accountNumber) {
        return getAccount(1, accountNumber);
    }

    public static CreateAccountResponse getAccount(int userNumber, int accountNumber) {
        List<CreateAccountResponse> accounts = getAccounts(userNumber);
        return accounts.get(accountNumber - 1);
    }

    //возможно пригодится для удаления всех акков
    public static List<CreateAccountResponse> getAllAccounts() {
        return INSTANCE.userAccounts.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    public static List<CreateAccountResponse> getAllAccountsForUser(User user) {
        return INSTANCE.userAccounts.get(user);
    }

    public static List<CreateAccountResponse> getAccounts(int number) {
        User user = getUser(number);
        return INSTANCE.userAccounts.get(user);
    }

    public static List<CreateAccountResponse> getAccounts() {
        return getAccounts(1);
    }

    public static void clear() {
        INSTANCE.userStepsMap.clear();
        INSTANCE.userAccounts.clear();
    }

}
