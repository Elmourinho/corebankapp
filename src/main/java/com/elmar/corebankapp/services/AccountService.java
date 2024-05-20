package com.elmar.corebankapp.services;

import com.elmar.corebankapp.models.requests.AccountRequest;
import com.elmar.corebankapp.models.responses.AccountResponse;

public interface AccountService {

    AccountResponse add(AccountRequest request);

    AccountResponse getById(Long id);
}
