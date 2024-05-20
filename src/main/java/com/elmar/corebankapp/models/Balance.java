package com.elmar.corebankapp.models;

import com.elmar.corebankapp.models.enums.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Balance {
    private Long id;
    private Long accountId;
    private Currency currency;
    private BigDecimal amount;
}
