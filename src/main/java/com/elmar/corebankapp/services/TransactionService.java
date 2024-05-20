package com.elmar.corebankapp.services;

import com.elmar.corebankapp.models.requests.TransactionRequest;
import com.elmar.corebankapp.models.responses.TransactionResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse add(TransactionRequest request);

    List<TransactionResponse> getByAccountId(Long accountId);
}
