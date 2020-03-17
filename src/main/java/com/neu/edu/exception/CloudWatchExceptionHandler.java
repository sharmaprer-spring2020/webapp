package com.neu.edu.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.neu.edu.UserController;
import com.timgroup.statsd.StatsDClientErrorHandler;

@Component
public class CloudWatchExceptionHandler implements StatsDClientErrorHandler {
	
	private final static Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Override
	public void handle(Exception exception) {
		logger.error(exception.getMessage());
	}

}
