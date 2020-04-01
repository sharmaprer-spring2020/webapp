package com.neu.edu.thread;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.edu.dao.BillDao;
import com.neu.edu.pojo.BillDueRequest;
import com.neu.edu.services.AWSQueueService;

import software.amazon.awssdk.services.sqs.model.Message;
//@Component
public class ProcessQueueMessage  {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessQueueMessage.class);

	@Autowired
	private BillDao billDao;
	
	//@Async
	public void processMessages() {
		logger.info("Started thread to poll SQS");
		while (true) {
			List<Message> message = AWSQueueService.readMessage();
			for (Message msg: message) {
				
				ObjectMapper objMapper = new ObjectMapper();
				try {
					logger.info(" Message Id ;" + msg.messageId());
					BillDueRequest bdr = objMapper.readValue(msg.body(), BillDueRequest.class);
					logger.info(" Message body ;" + msg.body());
					//List<BillDbEntity> billList = billDao.getByDueDate(bdr.getUserId(),LocalDate.now().plusDays(bdr.getDays()));
					//To SNS
					
					//delete each message from q
					logger.info(" Sending Request to delete message Id" + msg.messageId());
					AWSQueueService.deleteMessage(msg);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
				
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
