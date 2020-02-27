package com.neu.edu.services;

import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3Services {
	
	//private static S3Client s3 = S3Client.builder().region(Region.US_EAST_1).build();
	private static S3Client s3 = S3Client.builder().build();
	
	private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");
	public boolean addFile(byte[] fileBytes, String fileName){
		try {
			
			s3.putObject(PutObjectRequest.builder().bucket(BUCKET_NAME).key(fileName)
	                .build(),
	                RequestBody.fromBytes(fileBytes));
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean deleteFile(String fileName){
		try {
			System.out.println(fileName);
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(BUCKET_NAME).key(fileName).build();
	        s3.deleteObject(deleteObjectRequest);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
