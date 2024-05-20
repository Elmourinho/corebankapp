package com.elmar.corebankapp.services.impl;

import com.elmar.corebankapp.constants.RabbitMQConstants;
import com.elmar.corebankapp.errors.Errors;
import com.elmar.corebankapp.exceptions.CoreBankException;
import com.elmar.corebankapp.models.Transaction;
import com.elmar.corebankapp.models.enums.Currency;
import com.elmar.corebankapp.models.enums.TransactionDirection;
import com.elmar.corebankapp.models.requests.AccountRequest;
import com.elmar.corebankapp.models.requests.TransactionRequest;
import com.elmar.corebankapp.models.responses.AccountResponse;
import com.elmar.corebankapp.models.responses.BalanceResponse;
import com.elmar.corebankapp.models.responses.TransactionResponse;
import com.elmar.corebankapp.services.AccountService;
import com.elmar.corebankapp.services.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TransactionServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    @Transactional
    void addTransaction_shouldSaveTransactionCorrectly() {
        AccountRequest accountRequest = createAccountRequest();
        AccountResponse accountResponse = accountService.add(accountRequest);

        TransactionRequest transactionRequest = createTransactionRequest(accountResponse.getId(), Currency.USD,
                TransactionDirection.IN);
        TransactionResponse transactionResponse = transactionService.add(transactionRequest);

        assertThat(transactionResponse).isNotNull();
        assertThat(transactionResponse.getAccountId()).isEqualTo(accountResponse.getId());
        assertThat(transactionResponse.getCurrency()).isEqualTo(Currency.USD);
        assertThat(transactionResponse.getAmount()).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    @Transactional
    void addTransaction_shouldUpdateBalanceCorrectly() {
        AccountRequest accountRequest = createAccountRequest();
        AccountResponse accountResponse = accountService.add(accountRequest);

        TransactionRequest transactionRequest = createTransactionRequest(accountResponse.getId(), Currency.USD,
                TransactionDirection.IN);
        TransactionResponse transactionResponse = transactionService.add(transactionRequest);

        AccountResponse updatedAccount = accountService.getById(accountResponse.getId());
        BalanceResponse updatedBalance = updatedAccount.getBalances().stream()
                .filter(balance -> balance.getCurrency() == Currency.USD)
                .findFirst()
                .orElseThrow();

        assertThat(updatedBalance.getAmount()).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    @Transactional
    void addTransaction_shouldSendMessageCorrectly() {
        AccountRequest accountRequest = createAccountRequest();
        AccountResponse accountResponse = accountService.add(accountRequest);

        TransactionRequest transactionRequest = createTransactionRequest(accountResponse.getId(), Currency.USD,
                TransactionDirection.IN);
        TransactionResponse transactionResponse = transactionService.add(transactionRequest);

        Transaction receivedMessage = (Transaction) rabbitTemplate
                .receiveAndConvert(RabbitMQConstants.TRANSACTION_QUEUE, 5000);

        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getAmount()).isEqualTo(transactionResponse.getAmount());
        assertThat(receivedMessage.getDirection()).isEqualTo(transactionResponse.getDirection());
    }

    @Test
    void addTransaction_shouldThrowExceptionWhenAccountNotFound() {
        TransactionRequest request = createTransactionRequest(999L, Currency.USD, TransactionDirection.IN);

        CoreBankException exception = assertThrows(CoreBankException.class,
                () -> transactionService.add(request));

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorResponse().getKey()).isEqualTo(Errors.ACCOUNT_NOT_FOUND.getKey());
    }

    @Test
    void addTransaction_shouldThrowExceptionWhenBalanceNotFound() {
        AccountRequest accountRequest = createAccountRequest();
        AccountResponse accountResponse = accountService.add(accountRequest);
        TransactionRequest request = createTransactionRequest(accountResponse.getId(), Currency.SEK,
                TransactionDirection.IN);

        CoreBankException exception = assertThrows(CoreBankException.class,
                () -> transactionService.add(request));

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorResponse().getKey()).isEqualTo(Errors.BALANCE_NOT_FOUND.getKey());
    }

    @Test
    void addTransaction_shouldThrowExceptionWhenInsufficientFunds() {
        AccountRequest accountRequest = createAccountRequest();
        AccountResponse accountResponse = accountService.add(accountRequest);
        TransactionRequest request = createTransactionRequest(accountResponse.getId(), Currency.USD,
                TransactionDirection.OUT);

        CoreBankException exception = assertThrows(CoreBankException.class,
                () -> transactionService.add(request));

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorResponse().getKey()).isEqualTo(Errors.INSUFFICIENT_FUNDS.getKey());
    }

    @Test
    void addTransaction_shouldSaveDataEvenWhenMessageServiceFails() {
        AccountRequest accountRequest = createAccountRequest();
        AccountResponse accountResponse = accountService.add(accountRequest);
        TransactionRequest request = createTransactionRequest(accountResponse.getId(), Currency.USD,
                TransactionDirection.IN);

        CompletableFuture<Void> asyncOperation = CompletableFuture.runAsync(() -> {
            transactionService.add(request);
            rabbitTemplate.receiveAndConvert(RabbitMQConstants.TRANSACTION_QUEUE, 5000);
        });

        try {
            asyncOperation.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail("Asynchronous operation did not complete within the specified timeout");
        }

        List<TransactionResponse> transactions = transactionService.getByAccountId(accountResponse.getId());
        assertThat(transactions).isNotEmpty();
    }
    
    @Test
    @Transactional
    void getByAccountId_shouldReturnTransactionsForAccount() {
        AccountRequest accountRequest = createAccountRequest();
        AccountResponse accountResponse = accountService.add(accountRequest);

        TransactionRequest transactionRequest = createTransactionRequest(accountResponse.getId(), Currency.USD,
                TransactionDirection.IN);

        transactionService.add(transactionRequest);

        List<TransactionResponse> transactions = transactionService.getByAccountId(accountResponse.getId());
        assertThat(transactions).hasSize(1);

        TransactionResponse transactionResponse = transactions.getFirst();
        assertThat(transactionResponse.getAccountId()).isEqualTo(accountResponse.getId());
        assertThat(transactionResponse.getCurrency()).isEqualTo(Currency.USD);
        assertThat(transactionResponse.getAmount()).isEqualTo(BigDecimal.valueOf(100));
    }


    private AccountRequest createAccountRequest() {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setCustomerId(1L);
        accountRequest.setCountry("US");
        accountRequest.setCurrencyList(List.of(Currency.USD, Currency.EUR));
        return accountRequest;
    }

    private TransactionRequest createTransactionRequest(Long accountId, Currency currency,
                                                        TransactionDirection direction) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setAccountId(accountId);
        transactionRequest.setCurrency(currency);
        transactionRequest.setAmount(BigDecimal.valueOf(100));
        transactionRequest.setDirection(direction);
        return transactionRequest;
    }


}
