package com.elmar.corebankapp.models.responses;

import com.elmar.corebankapp.models.enums.Currency;
import com.elmar.corebankapp.models.enums.TransactionDirection;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionResponse {
    private Long transactionId;
    private Long accountId;
    private BigDecimal amount;
    private Currency currency;
    private TransactionDirection direction;
    private String description;
}
