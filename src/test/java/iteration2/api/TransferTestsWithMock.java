package iteration2.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.entities.User;
import api.generators.RandomData;
import api.generators.TransferRequestGenerator;
import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import base.BaseTest;
import common.annotations.APIVersion;
import common.annotations.FraudCheckMock;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.TimingExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.within;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferTestsWithMock extends BaseTest {

    @APIVersion("with_fraud_check_with_transfer_fix")
    @DisplayName("User can transfer money between accounts, low risk fraud")
    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void userCanTransferMoneyBetweenAccountsLowRiskTransition() {
        double depositAmount = RandomData.getRandomValidDepositAmount();
        double transferAmount = RandomData.getRandomValidTransferLessOrEqualDeposit(depositAmount);
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse account = UserSteps.createAccount(user.getRequest());
        long senderAccountId = account.getId();
        CreateAccountResponse accountSecond = UserSteps.createAccount(user.getRequest());
        long receiverAccountId = accountSecond.getId();
        AccountSteps accountSteps = new AccountSteps(user.getRequest().getUsername(), user.getRequest().getPassword());

        accountSteps.assertAccountDBInfo(user, account);
        accountSteps.assertAccountDBInfo(user, accountSecond);

        GetUserAccountsResponse beforeTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse beforeTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), accountSecond.getId());

        softly.assertThat(beforeTransferSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        accountSteps.depositToAccount(senderAccountId, depositAmount);

        accountSteps.assertAccountDBInfo(user, account);
        accountSteps.assertAccountDBInfo(user, accountSecond);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), accountSecond.getId());

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositAmount);
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequestGenerator.makeRequest(senderAccountId, receiverAccountId, transferAmount);
        TransferResponse transferResponse = UserSteps.makeTransferWithFraudCheck(user, transferRequest);

        TransferResponse expectedResponse = TransferResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(transferAmount)
                .senderAccountId(account.getId())
                .receiverAccountId(accountSecond.getId())
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();

        AccountDao accountDaoAfterTransfer = DataBaseSteps.getAccountByAccountNumber(accountSecond.getAccountNumber());
        DaoAndModelAssertions.assertThat(transferResponse, accountDaoAfterTransfer).match();
        ModelAssertions.assertThatModels(transferRequest, transferResponse).match();

        softly.assertThat(accountDaoAfterTransfer.getBalance())
                .isCloseTo(transferAmount, within(0.01));

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        double accBalanceDB = DataBaseSteps.getBalanceByAccountNumber(account.getAccountNumber());
        double accBalanceSecondDB = DataBaseSteps.getBalanceByAccountNumber(accountSecond.getAccountNumber());

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isCloseTo(depositAmount - transferRequest.getAmount(), within(0.01));
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isCloseTo(RequestSpecs.INITIAL_BALANCE + transferRequest.getAmount(), within(0.01));

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(accBalanceDB);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(accBalanceSecondDB);

        List<GetTransitionsResponse> transactionByAccount = UserSteps.getTransitions(user, senderAccountId);
        long transactionId = transactionByAccount.getFirst().getId();
        FraudCheckResponse fraudCheckResponse = accountSteps.getFraudCheckResult(transactionId);

        softly.assertThat(fraudCheckResponse.getStatus())
                .isEqualTo(ResponseSpecs.WITHOUT_CHECKING_FRAUD_STATUS);
        softly.assertThat(fraudCheckResponse.getNote())
                .isEqualTo(ResponseSpecs.WITHOUT_CHECKING_FRAUD_MESSAGE);
    }

    @APIVersion("with_fraud_check_with_transfer_fix")
    @DisplayName("User can transfer money between accounts, medium risk fraud")
    @Test
    @FraudCheckMock(
            status = "FAILED",
            decision = "DECLINED",
            riskScore = 0.6,
            reason = "Medium risk transaction",
            requiresManualReview = true,
            additionalVerificationRequired = false
    )

    public void userCanTransferMoneyBetweenAccountsMediumRiskTransition() {
        double depositAmount = RandomData.getRandomValidDepositAmount();
        double transferAmount = RandomData.getRandomValidTransferLessOrEqualDeposit(depositAmount);
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse account = UserSteps.createAccount(user.getRequest());
        long senderAccountId = account.getId();
        CreateAccountResponse accountSecond = UserSteps.createAccount(user.getRequest());
        long receiverAccountId = accountSecond.getId();
        AccountSteps accountSteps = new AccountSteps(user.getRequest().getUsername(), user.getRequest().getPassword());

        accountSteps.assertAccountDBInfo(user, account);
        accountSteps.assertAccountDBInfo(user, accountSecond);

        GetUserAccountsResponse beforeTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse beforeTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), accountSecond.getId());

        softly.assertThat(beforeTransferSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        accountSteps.depositToAccount(senderAccountId, depositAmount);

        accountSteps.assertAccountDBInfo(user, account);
        accountSteps.assertAccountDBInfo(user, accountSecond);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), accountSecond.getId());

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositAmount);
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequestGenerator.makeRequest(senderAccountId, receiverAccountId, transferAmount);
        TransferResponse transferResponse = UserSteps.makeTransferWithFraudCheck(user, transferRequest);

        TransferResponse expectedResponse = TransferResponse.builder()
                .status("MANUAL_REVIEW_REQUIRED")
                .message("Transfer requires manual review")
                .amount(transferAmount)
                .senderAccountId(account.getId())
                .receiverAccountId(accountSecond.getId())
                .fraudRiskScore(0.6)
                .fraudReason("Medium risk transaction")
                .requiresManualReview(true)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositAmount);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        List<GetTransitionsResponse> transactionByAccount = UserSteps.getTransitions(user, senderAccountId);
        long transactionId = transactionByAccount.getFirst().getId();
        FraudCheckResponse fraudCheckResponse = accountSteps.getFraudCheckResult(transactionId);

        softly.assertThat(fraudCheckResponse.getStatus())
                .isEqualTo(ResponseSpecs.WITHOUT_CHECKING_FRAUD_STATUS);
        softly.assertThat(fraudCheckResponse.getNote())
                .isEqualTo(ResponseSpecs.WITHOUT_CHECKING_FRAUD_MESSAGE);
    }

    @APIVersion("with_fraud_check_with_transfer_fix")
    @DisplayName("User can transfer money between accounts, high risk fraud")
    @Test
    @FraudCheckMock(
            status = "FAILED",
            decision = "DECLINED",
            riskScore = 1.0,
            reason = "High risk transaction",
            requiresManualReview = true,
            additionalVerificationRequired = true
    )

    public void userCanTransferMoneyBetweenAccountsHighRiskTransition() {
        double depositAmount = RandomData.getRandomValidDepositAmount();
        double transferAmount = RandomData.getRandomValidTransferLessOrEqualDeposit(depositAmount);
        User user = AdminSteps.createUser();
        usersToDelete.add(user.getResponse().getId());

        CreateAccountResponse account = UserSteps.createAccount(user.getRequest());
        long senderAccountId = account.getId();
        CreateAccountResponse accountSecond = UserSteps.createAccount(user.getRequest());
        long receiverAccountId = accountSecond.getId();
        AccountSteps accountSteps = new AccountSteps(user.getRequest().getUsername(), user.getRequest().getPassword());

        accountSteps.assertAccountDBInfo(user, account);
        accountSteps.assertAccountDBInfo(user, accountSecond);

        GetUserAccountsResponse beforeTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse beforeTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), accountSecond.getId());

        softly.assertThat(beforeTransferSenderAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);
        softly.assertThat(beforeTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        accountSteps.depositToAccount(senderAccountId, depositAmount);

        accountSteps.assertAccountDBInfo(user, account);
        accountSteps.assertAccountDBInfo(user, accountSecond);

        GetUserAccountsResponse afterDepositSenderAccount = UserSteps.getAccountById(user.getRequest(), account.getId());
        GetUserAccountsResponse afterDepositReceiverAccount = UserSteps.getAccountById(user.getRequest(), accountSecond.getId());

        softly.assertThat(afterDepositSenderAccount.getBalance())
                .isEqualTo(depositAmount);
        softly.assertThat(afterDepositReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        TransferRequest transferRequest = TransferRequestGenerator.makeRequest(senderAccountId, receiverAccountId, transferAmount);
        TransferResponse transferResponse = UserSteps.makeTransferWithFraudCheck(user, transferRequest);

        TransferResponse expectedResponse = TransferResponse.builder()
                .status("VERIFICATION_REQUIRED")
                .message("Additional verification required")
                .amount(transferAmount)
                .senderAccountId(account.getId())
                .receiverAccountId(accountSecond.getId())
                .fraudRiskScore(1.0)
                .fraudReason("High risk transaction")
                .requiresManualReview(true)
                .requiresVerification(true)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();

        GetUserAccountsResponse afterTransferSenderAccount = UserSteps.getAccountById(user.getRequest(), senderAccountId);
        GetUserAccountsResponse afterTransferReceiverAccount = UserSteps.getAccountById(user.getRequest(), receiverAccountId);

        softly.assertThat(afterTransferSenderAccount.getBalance())
                .isEqualTo(depositAmount);
        softly.assertThat(afterTransferReceiverAccount.getBalance())
                .isEqualTo(RequestSpecs.INITIAL_BALANCE);

        List<GetTransitionsResponse> transactionByAccount = UserSteps.getTransitions(user, senderAccountId);
        long transactionId = transactionByAccount.getFirst().getId();
        FraudCheckResponse fraudCheckResponse = accountSteps.getFraudCheckResult(transactionId);

        //в данный момент работает так..
        softly.assertThat(fraudCheckResponse.getStatus())
                .isEqualTo(ResponseSpecs.WITHOUT_CHECKING_FRAUD_STATUS);
        softly.assertThat(fraudCheckResponse.getNote())
                .isEqualTo(ResponseSpecs.WITHOUT_CHECKING_FRAUD_MESSAGE);
    }
}
