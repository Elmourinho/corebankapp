package com.elmar.corebankapp.errors;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum Errors implements ErrorResponse{

	ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", HttpStatus.NOT_FOUND, "Account with id {id} not found"),
	BALANCE_NOT_FOUND("BALANCE_NOT_FOUND", HttpStatus.NOT_FOUND, "Balance with currency {currency} not found"),
	INSUFFICIENT_FUNDS("INSUFFICIENT_FUNDS", HttpStatus.BAD_REQUEST, "Balance amount is less than required amount"),;

	final String key;
	final HttpStatus httpStatus;
	final String message;

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
}
