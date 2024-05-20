package com.elmar.corebankapp.errors;

import org.springframework.http.HttpStatus;

public interface ErrorResponse {

	String getKey();

	String getMessage();

	HttpStatus getHttpStatus();
}
