package com.elmar.corebankapp.models.responses;

import com.elmar.corebankapp.models.enums.Currency;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceResponse {
    private Currency currency;
    private BigDecimal amount;
}
