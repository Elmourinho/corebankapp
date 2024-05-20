package com.elmar.corebankapp.models.responses;

import lombok.Data;

import java.util.List;

@Data
public class AccountResponse {
    private Long id;
    private Long customerId;
    private List<BalanceResponse> balances;
}
