package com.elmar.corebankapp.services.impl;

import com.elmar.corebankapp.constants.RabbitMQConstants;
import com.elmar.corebankapp.errors.Errors;
import com.elmar.corebankapp.exceptions.CoreBankException;
import com.elmar.corebankapp.models.Account;
import com.elmar.corebankapp.models.enums.Currency;
import com.elmar.corebankapp.models.requests.AccountRequest;
import com.elmar.corebankapp.models.responses.AccountResponse;
import com.elmar.corebankapp.models.responses.BalanceResponse;
import com.elmar.corebankapp.services.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AccountServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    @Transactional
    void addAccount_shouldSaveDataCorrectly() {
        AccountRequest accountRequest = createAccountRequest();

        AccountResponse accountResponse = accountService.add(accountRequest);

        assertThat(accountResponse).isNotNull();
        assertThat(accountResponse.getCustomerId()).isEqualTo(1L);
        assertThat(accountResponse.getBalances()).hasSize(2);

        BalanceResponse balance1 = accountResponse.getBalances().getFirst();
        assertThat(balance1.getCurrency()).isEqualTo(Currency.USD);
        assertThat(balance1.getAmount()).isEqualTo(BigDecimal.ZERO);

        BalanceResponse balance2 = accountResponse.getBalances().get(1);
        assertThat(balance2.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(balance2.getAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @Transactional
    void addAccount_shouldSendMessage() {
        AccountRequest accountRequest = createAccountRequest();

        AccountResponse accountResponse = accountService.add(accountRequest);

        Account receivedMessage = (Account) rabbitTemplate
                .receiveAndConvert(RabbitMQConstants.ACCOUNT_QUEUE, 5000);
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getId()).isEqualTo(accountResponse.getId());
        assertThat(receivedMessage.getBalances()).hasSize(2);

        BalanceResponse receivedBalance1 = accountResponse.getBalances().getFirst();
        assertThat(receivedBalance1.getCurrency()).isEqualTo(Currency.USD);
        assertThat(receivedBalance1.getAmount()).isEqualTo(BigDecimal.ZERO);

        BalanceResponse receivedBalance2 = accountResponse.getBalances().get(1);
        assertThat(receivedBalance2.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(receivedBalance2.getAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @Transactional
    void addAccount_shouldSaveDataCorrectlyWhenMessageServiceFails() {

        AccountRequest accountRequest = createAccountRequest();

        AccountResponse accountResponse = accountService.add(accountRequest);

        CompletableFuture<Void> asyncOperation = CompletableFuture.runAsync(() -> {
            Account receivedMessage = (Account) rabbitTemplate.receiveAndConvert(RabbitMQConstants.ACCOUNT_QUEUE, 5000);
            assertThat(receivedMessage).isNotNull();
            assertThat(receivedMessage.getId()).isEqualTo(accountResponse.getId());
        });

        try {
            asyncOperation.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail("Asynchronous operation did not complete within the specified timeout");
        }

        AccountResponse savedAccount = accountService.getById(accountResponse.getId());
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getCustomerId()).isEqualTo(accountRequest.getCustomerId());
        assertThat(savedAccount.getBalances()).hasSize(2);

        BalanceResponse receivedBalance1 = savedAccount.getBalances().getFirst();
        assertThat(receivedBalance1.getCurrency()).isEqualTo(Currency.USD);
        assertThat(receivedBalance1.getAmount()).isEqualTo(BigDecimal.ZERO);

        BalanceResponse receivedBalance2 = savedAccount.getBalances().get(1);
        assertThat(receivedBalance2.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(receivedBalance2.getAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getById_shouldReturnCorrectlySavedAccount() {
        AccountRequest accountRequest = createAccountRequest();

        AccountResponse addedAccount = accountService.add(accountRequest);
        AccountResponse retrievedAccount = accountService.getById(addedAccount.getId());

        assertThat(retrievedAccount).isNotNull();
        assertThat(retrievedAccount.getId()).isEqualTo(addedAccount.getId());
        assertThat(retrievedAccount.getCustomerId()).isEqualTo(addedAccount.getCustomerId());

        BalanceResponse receivedBalance1 = retrievedAccount.getBalances().getFirst();
        assertThat(receivedBalance1.getCurrency()).isEqualTo(Currency.USD);
        assertThat(receivedBalance1.getAmount()).isEqualTo(BigDecimal.ZERO);

        BalanceResponse receivedBalance2 = retrievedAccount.getBalances().get(1);
        assertThat(receivedBalance2.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(receivedBalance2.getAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getById_shouldThrowExceptionWhenAccountNotFound() {
        Long nonExistentAccountId = 999L;

        CoreBankException exception = assertThrows(CoreBankException.class,
                () -> accountService.getById(nonExistentAccountId));

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorResponse().getKey()).isEqualTo(Errors.ACCOUNT_NOT_FOUND.getKey());
    }

    private AccountRequest createAccountRequest() {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setCustomerId(1L);
        accountRequest.setCountry("US");
        accountRequest.setCurrencyList(List.of(Currency.USD, Currency.EUR));
        return accountRequest;
    }
}
