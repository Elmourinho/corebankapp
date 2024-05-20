package com.elmar.corebankapp.services.impl;

import com.elmar.corebankapp.components.LockManager;
import com.elmar.corebankapp.constants.RabbitMQConstants;
import com.elmar.corebankapp.errors.Errors;
import com.elmar.corebankapp.exceptions.CoreBankException;
import com.elmar.corebankapp.mappers.AccountMapper;
import com.elmar.corebankapp.mappers.BalanceMapper;
import com.elmar.corebankapp.mappers.TransactionMapper;
import com.elmar.corebankapp.models.Transaction;
import com.elmar.corebankapp.models.enums.TransactionDirection;
import com.elmar.corebankapp.models.requests.TransactionRequest;
import com.elmar.corebankapp.models.responses.TransactionResponse;
import com.elmar.corebankapp.services.MessageService;
import com.elmar.corebankapp.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountMapper accountMapper;
    private final BalanceMapper balanceMapper;
    private final TransactionMapper transactionMapper;
    private final ModelMapper modelMapper;
    private final LockManager lockManager;
    private final MessageService messageService;

    @Transactional
    @Override
    public TransactionResponse add(TransactionRequest request) {

        var id = request.getAccountId();
        lockManager.lock(id);
        try {
            return processTransaction(request, id);
        } finally {
            lockManager.unlock(id);
            lockManager.removeLock(id);
        }

    }

    private TransactionResponse processTransaction(TransactionRequest request, Long accountId) {
        var account = accountMapper.getById(accountId)
                .orElseThrow(() -> new CoreBankException(Errors.ACCOUNT_NOT_FOUND, Map.of("id", accountId)));

        var currency = request.getCurrency();
        var balance = account.getBalanceWithCurrency(currency)
                .orElseThrow(() -> new CoreBankException(Errors.BALANCE_NOT_FOUND, Map.of("currency", currency)));

        if (TransactionDirection.OUT == request.getDirection()
                && balance.getAmount().compareTo(request.getAmount()) < 0) {
            throw new CoreBankException(Errors.INSUFFICIENT_FUNDS);
        }

        var newAmount = TransactionDirection.IN == request.getDirection()
                ? balance.getAmount().add(request.getAmount())
                : balance.getAmount().subtract(request.getAmount());

        balance.setAmount(newAmount);
        balanceMapper.updateBalance(balance);

        Transaction transaction = modelMapper.map(request, Transaction.class);
        transactionMapper.insertTransaction(transaction);
        messageService.sendMessageAsync(RabbitMQConstants.TRANSACTION_QUEUE, transaction);
        return modelMapper.map(transaction, TransactionResponse.class);
    }

    @Override
    public List<TransactionResponse> getByAccountId(Long accountId) {
        List<Transaction> transactions = transactionMapper.getByAccountId(accountId);
        return transactions.stream()
                .map(transaction -> modelMapper.map(transaction, TransactionResponse.class))
                .toList();
    }

}
