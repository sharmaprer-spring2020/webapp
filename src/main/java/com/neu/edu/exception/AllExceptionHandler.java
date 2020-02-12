package com.neu.edu.exception;

import java.sql.SQLException;
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
	
	@ExceptionHandler(FileException.class)
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	public @ResponseBody ExceptionResponse handleFileException(FileException ex, HttpServletRequest request  ) {
		
		ExceptionResponse error= new ExceptionResponse();
		error.setMessage(ex.getMessage());
		
		error.setPath(request.getRequestURI());
		error.setTimestamp(LocalDateTime.now());
		error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		
		return error;
	}
	
	@ExceptionHandler(SQLException.class)
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	public @ResponseBody ExceptionResponse sqlError(SQLException ex, HttpServletRequest request  ) {
		
		ExceptionResponse error= new ExceptionResponse();
		error.setMessage(ex.getMessage());
		
		error.setPath(request.getRequestURI());
		error.setTimestamp(LocalDateTime.now());
		error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		
		return error;
	}
	
	@ExceptionHandler(ValidationException.class)
	@ResponseStatus(value=HttpStatus.UNAUTHORIZED)
	public @ResponseBody ExceptionResponse handleValidationException(ValidationException ex, HttpServletRequest request ) {
		
		ExceptionResponse error= new ExceptionResponse();
		error.setMessage(ex.getMessage());
		
		error.setPath(request.getRequestURI());
		error.setTimestamp(LocalDateTime.now());
		error.setStatus(HttpStatus.UNAUTHORIZED);
		
		return error;
	}
	
	@ExceptionHandler(IdValidationException.class)
	@ResponseStatus(value=HttpStatus.NOT_FOUND)
	public @ResponseBody ExceptionResponse handleBillValidationException(IdValidationException ex, HttpServletRequest request ) {
		
		ExceptionResponse error= new ExceptionResponse();
		error.setMessage(ex.getMessage());
		
		error.setPath(request.getRequestURI());
		error.setTimestamp(LocalDateTime.now());
		error.setStatus(HttpStatus.NOT_FOUND);
		
		return error;
	}
	
	@ExceptionHandler(FileTypeException.class)
	@ResponseStatus(value=HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	public @ResponseBody ExceptionResponse fileTypeValidation(FileTypeException ex, HttpServletRequest request ) {
		
		ExceptionResponse error= new ExceptionResponse();
		error.setMessage(ex.getMessage());
		
		error.setPath(request.getRequestURI());
		error.setTimestamp(LocalDateTime.now());
		error.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
		
		return error;
	}
	
	@ExceptionHandler(FileExistsExeption.class)
	@ResponseStatus(value=HttpStatus.BAD_REQUEST)
	public @ResponseBody ExceptionResponse fileExists(FileExistsExeption ex, HttpServletRequest request ) {
		
		ExceptionResponse error= new ExceptionResponse();
		error.setMessage(ex.getMessage());
		
		error.setPath(request.getRequestURI());
		error.setTimestamp(LocalDateTime.now());
		error.setStatus(HttpStatus.BAD_REQUEST);
		
		return error;
	}
	
}
