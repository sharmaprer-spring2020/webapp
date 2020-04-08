package com.neu.edu.thread;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.edu.dao.BillDao;
import com.neu.edu.pojo.BillDbEntity;
import com.neu.edu.pojo.BillDueRequest;
import com.neu.edu.pojo.NotificationMessage;
import com.neu.edu.services.AWSNotificationService;
import com.neu.edu.services.AWSQueueService;

import software.amazon.awssdk.services.sqs.model.Message;

//@Component
public class ProcessQueueMessage implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessQueueMessage.class);

	private BillDao billDao;
	
	private volatile boolean continuePoll = true;
	
	public ProcessQueueMessage(BillDao billDao) {
		this.billDao = billDao;
	}
	//@Async
	@Override
	public void run() {
		logger.info("Started thread to poll SQS");
		while (continuePoll) {
			List<Message> message = AWSQueueService.readMessage();
			for (Message msg: message) {
				
				ObjectMapper objMapper = new ObjectMapper();
				try {
					logger.info(" Message Id ;" + msg.messageId());
					BillDueRequest bdr = objMapper.readValue(msg.body(), BillDueRequest.class);
					logger.info(" Message body ;" + msg.body());
					System.out.println("UserID from msg body user Id: "+bdr.getUserId());
					System.out.println("UserID from msg body days: "+bdr.getDays());
					LocalDate currentDate = LocalDate.now();
					List<BillDbEntity> billList = billDao.getByDueDate(bdr.getUserId(),LocalDate.now().plusDays(bdr.getDays()),currentDate);
					//To SNS
					//if (!billList.isEmpty()) {
						NotificationMessage nm = new NotificationMessage();
						nm.setUserId(bdr.getUserId());
						nm.setEmailId(bdr.getEmailId());
						nm.setUrls(new ArrayList<String>());
						for (BillDbEntity bill : billList) {
							nm.getUrls().add(bill.getId());
						}
						ObjectMapper om  = new ObjectMapper();
						AWSNotificationService.pushMessage(om.writeValueAsString(nm));
					//}
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
	
	public void stop() {
		continuePoll = false;

	}

}
