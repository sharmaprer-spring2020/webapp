package com.neu.edu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neu.edu.exception.QueriesException;
import com.neu.edu.services.MessagePollerService;


@RestController
public class MessageProcessorController {

	private static final Logger logger = LoggerFactory.getLogger(MessageProcessorController.class); 
	
	@Autowired
	private MessagePollerService mps;
	
	@GetMapping(path ="/v1/sqs-poll/start")
	public ResponseEntity<?> start() throws QueriesException{
		mps.startPolling();
		return new ResponseEntity<>(HttpStatus.OK);
		
	}
	
	@GetMapping(path ="/v1/sqs-poll/stop")
	public ResponseEntity<?> stop() throws QueriesException{
		mps.stopPolling();
		return new ResponseEntity<>(HttpStatus.OK);
		
	}
	
}
