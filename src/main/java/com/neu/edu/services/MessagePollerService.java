package com.neu.edu.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.neu.edu.dao.BillDao;
import com.neu.edu.thread.ProcessQueueMessage;

@Component
//public class ThreadRunner implements CommandLineRunner {
public class MessagePollerService {
	
	private static final Logger logger = LoggerFactory.getLogger(MessagePollerService.class);
	
	@Autowired
	private BillDao billDao;
	private volatile Thread pqm;
	private ProcessQueueMessage processMsg;
	public void startPolling() {
		if (pqm == null || !pqm.isAlive()) {
			processMsg = new ProcessQueueMessage(billDao);
			pqm = new Thread(processMsg);
			logger.info("Starting polling thread for SQS");
			pqm.start();
		} else {
			logger.info("Already Polling");
		}
	}
	
	public  void stopPolling() {
		if(pqm != null && pqm.isAlive()) {
			logger.info("Stopping polling thread for SQS");
			processMsg.stop();
		}
	}

}
