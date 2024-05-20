package com.elmar.corebankapp.models;

import com.elmar.corebankapp.models.enums.Currency;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class Account {
    private Long id;
    private Long customerId;
    private String country;
    private List<Balance> balances;

    public Optional<Balance> getBalanceWithCurrency(Currency givenCurrency) {
        return balances.stream()
                .filter(balance -> balance.getCurrency() == givenCurrency)
                .findFirst();
    }
}
