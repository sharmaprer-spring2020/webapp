package com.neu.edu.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ListTopicsResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

@Component
public class AWSNotificationService {
	private static final Logger logger = LoggerFactory.getLogger(AWSNotificationService.class);
	
	private static final SnsClient SNS_CLIENT = SnsClient.builder()
            .region(Region.US_EAST_1)
            .build();
	@Value("${SNS.topic:csyeLambda_topic}")
	private static String topicArn;
	
	public static boolean pushMessage(String message) {

		try {
			SnsClient snsClient = SnsClient.builder().region(Region.US_EAST_1).build();
			ListTopicsResponse lrt = snsClient.listTopics();
			String topicArn = lrt.topics().get(0).topicArn();

			PublishRequest request = PublishRequest.builder().message(message).topicArn(topicArn).build();

			PublishResponse result = SNS_CLIENT.publish(request);
			logger.debug("Message sent to SNS :" + result.messageId() + " \n" + message);

		} catch (SnsException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		
		return true;
	}

}
