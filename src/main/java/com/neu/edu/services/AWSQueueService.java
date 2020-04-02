package com.neu.edu.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueNameExistsException;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class AWSQueueService {

	private static final Logger logger = LoggerFactory.getLogger(AWSQueueService.class);
	
	//private static final String QUEUE_NAME = System.getenv("QUEUE_NAME");
	private static final String QUEUE_NAME =  System.getenv("QUEUE_NAME"); //"webapp.fifo";
	private static final SqsClient SQS_CLIENT = SqsClient.builder()
            .region(Region.US_EAST_1)
            .build();
	
	public static boolean sendMessage(String message) {
		System.out.println("Inside send message due"+message);
        try {
        	
        	GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                    .queueName(QUEUE_NAME)
                    .build();
            String queueUrl = SQS_CLIENT.getQueueUrl(getQueueRequest).queueUrl();
                
        	logger.debug("AWSQueueService sendMessage() : " + message);
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .messageGroupId("dev-test")
                .build();
            System.out.println("After sendMsgRequest");
            SQS_CLIENT.sendMessage(sendMsgRequest);
            System.out.println("After sendMsg");
            return true;
        } catch (QueueNameExistsException e) {
        	logger.error(e.getMessage(), e);
            return false;
        }
	}
	
	public static List<Message> readMessage() {
		logger.debug("AWSQueueService readMessage() request");
		GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(QUEUE_NAME)
                .build();
        String queueUrl = SQS_CLIENT.getQueueUrl(getQueueRequest).queueUrl();
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)
                //.visibilityTimeout(5)
                .build();
        List<Message> messages= SQS_CLIENT.receiveMessage(receiveMessageRequest).messages();
        logger.debug("AWSQueueService readMessage() response messages list size :" + messages.size());
        if (messages!=null && messages.size() > 0) {
        	for(Message msg : messages) {
        		logger.debug("AWSQueueService readMessage() response messages list size :" + msg.body());
        	}
        }
		return messages;
	}
	
	public static boolean deleteMessage(Message message) {
		try {
			GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                    .queueName(QUEUE_NAME)
                    .build();
            String queueUrl = SQS_CLIENT.getQueueUrl(getQueueRequest).queueUrl();
			DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
					.queueUrl(queueUrl)
					.receiptHandle(message.receiptHandle())
					.build();
			SQS_CLIENT.deleteMessage(deleteMessageRequest);    	
			return true;
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
            return false;
		}
	}
	
}
