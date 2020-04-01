package com.neu.edu.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.neu.edu.thread.ProcessQueueMessage;

@Component
//public class ThreadRunner implements CommandLineRunner {
public class MessagePollerService {
	
	private static final Logger logger = LoggerFactory.getLogger(MessagePollerService.class);
	
	private Thread pqm;
	private ProcessQueueMessage processMsg;
	public void startPolling() {
		if (pqm == null) {
			logger.info("Starting polling thread for SQS");
			processMsg = new ProcessQueueMessage();
			pqm = new Thread(processMsg);
			pqm.start();
		} else {
			stopPolling();
			try {
				Thread.sleep(6000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
			startPolling();
		}
	}
	
	public  void stopPolling() {
		if(pqm != null && pqm.isAlive()) {
			logger.info("Stopping polling thread for SQS");
			processMsg.stop();
		}
	}

}
