package com.elmar.corebankapp.services.impl;

import com.elmar.corebankapp.constants.RabbitMQConstants;
import com.elmar.corebankapp.errors.Errors;
import com.elmar.corebankapp.exceptions.CoreBankException;
import com.elmar.corebankapp.mappers.AccountMapper;
import com.elmar.corebankapp.mappers.BalanceMapper;
import com.elmar.corebankapp.models.Account;
import com.elmar.corebankapp.models.Balance;
import com.elmar.corebankapp.models.enums.Currency;
import com.elmar.corebankapp.models.requests.AccountRequest;
import com.elmar.corebankapp.models.responses.AccountResponse;
import com.elmar.corebankapp.services.AccountService;
import com.elmar.corebankapp.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final BalanceMapper balanceMapper;
    private final ModelMapper modelMapper;
    private final MessageService messageService;

    @Override
    @Transactional
    public AccountResponse add(AccountRequest accountRequest) {
        var account = saveAccount(accountRequest);
        var balances = saveBalances(account.getId(), accountRequest.getCurrencyList());
        account.setBalances(balances);
        messageService.sendMessageAsync(RabbitMQConstants.ACCOUNT_QUEUE, account);
        return modelMapper.map(account, AccountResponse.class);
    }

    @Override
    public AccountResponse getById(Long id) {
        var account = accountMapper.getById(id)
                .orElseThrow(() -> new CoreBankException(Errors.ACCOUNT_NOT_FOUND, Map.of("id", id)));
        return modelMapper.map(account, AccountResponse.class);
    }

    private Account saveAccount(AccountRequest request) {
        var account = modelMapper.map(request, Account.class);
        accountMapper.insert(account);
        return account;
    }

    private List<Balance> saveBalances(Long accountId, List<Currency> currencyList) {
        return currencyList.stream()
                .map(currency -> {
                    Balance balance = Balance.builder()
                            .accountId(accountId)
                            .currency(currency)
                            .amount(BigDecimal.ZERO)
                            .build();
                    balanceMapper.insert(balance);
                    return balance;
                })
                .toList();
    }

}