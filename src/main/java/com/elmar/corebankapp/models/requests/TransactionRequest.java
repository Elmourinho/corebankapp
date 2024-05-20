package com.elmar.corebankapp.models.requests;

import com.elmar.corebankapp.models.enums.Currency;
import com.elmar.corebankapp.models.enums.TransactionDirection;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequest {

    @NotNull
    private Long accountId;

    @NotNull
    @Positive
    private BigDecimal amount;

    private Currency currency;

    private TransactionDirection direction;

    @NotNull
    @NotEmpty
    private String description;
}
