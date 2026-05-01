package api.requests.steps;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.entities.User;
import api.generators.DepositRequestGenerator;
import api.models.*;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.helpers.StepLogger;
import io.qameta.allure.Step;

public class AccountSteps {
    private String username;
    private String password;

    public AccountSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public DepositResponse depositToAccount(long accountId, double amount) {
        return StepLogger.log("User " + username + " deposits " + amount + " to account " + accountId, () -> {
            DepositRequest depositRequest = DepositRequestGenerator.withAmount(accountId, amount);

            return new ValidatedCrudRequester<DepositResponse>(
                    RequestSpecs.authAsUser(username, password),
                    Endpoint.DEPOSIT,
                    ResponseSpecs.requestReturnsOK()).post(depositRequest);
        });
    }

    @Step("Assert account in database")
    public void assertAccountDBInfo(User user, CreateAccountResponse account) {
        GetUserAccountsResponse accountById = UserSteps.getAccountById(user.getRequest(), account.getId());
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(account.getAccountNumber());
        DaoAndModelAssertions.assertThat(accountById, accountDao).match();
    }

    public FraudCheckResponse getFraudCheckResult(long transactionId) {
        return new ValidatedCrudRequester<FraudCheckResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.FRAUD_CHECK_STATUS,
                ResponseSpecs.requestReturnsOK()).get("transactionId", transactionId);
    }
}
