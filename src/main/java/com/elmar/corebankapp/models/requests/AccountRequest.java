package com.elmar.corebankapp.models.requests;

import com.elmar.corebankapp.models.enums.Currency;
import lombok.Data;

import java.util.List;

@Data
public class AccountRequest {
    private Long customerId;
    private String country;
    private List<Currency> currencyList;
}
