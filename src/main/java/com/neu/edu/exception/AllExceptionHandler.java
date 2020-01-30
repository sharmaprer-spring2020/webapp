package com.neu.edu.exception;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
public class AllExceptionHandler {
	
	@ExceptionHandler(QueriesException.class)
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	public @ResponseBody ExceptionResponse handleSQLException(QueriesException ex, HttpServletRequest request  ) {
		
		ExceptionResponse error= new ExceptionResponse();
		error.setMessage(ex.getMessage());
		
		error.setPath(request.getRequestURI());
		error.setTimestamp(LocalDateTime.now());
		error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		
		return error;
	}

}
