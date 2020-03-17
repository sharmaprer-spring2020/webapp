package com.neu.edu;

import java.time.LocalDateTime;
import java.util.Base64;

import javax.validation.Valid;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.neu.edu.dao.UserDao;
import com.neu.edu.exception.CloudWatchExceptionHandler;
import com.neu.edu.exception.QueriesException;
import com.neu.edu.pojo.User;
import com.timgroup.statsd.NonBlockingStatsDClient;

@RestController
public class UserController {
	
	private final static Logger logger = LoggerFactory.getLogger(UserController.class);
	private static final int STATSD_SERVER_PORT = 8125;

	
	@Autowired
	UserDao userDao;
	
	@Autowired
	CloudWatchExceptionHandler cloudWatchHandler;
	
	private final NonBlockingStatsDClient client = new NonBlockingStatsDClient("my.prefix", "3.234.228.239", STATSD_SERVER_PORT, cloudWatchHandler);
	//@Autowired
	//private StatsDClient statsDClient;
	
	//Get user Information
	@GetMapping(path ="v1/user/self", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getUserInfo(@RequestHeader(value = "Authorization",required=false) String authToken) throws QueriesException{
		//logger.warn("This is prerna sharma");
		//logger.debug("This is prerna sharma debug");
		logger.trace("This is prerna sharma trace");
		
		//statsDClient.incrementCounter("getUser");
		User authenticatedUser = checkAuthentication(authToken);
		logger.warn("This is prerna sharma");
		
		logger.warn("This is prerna sharma in trace mode");
		if(authenticatedUser != null) {
			return new ResponseEntity<>(authenticatedUser,HttpStatus.OK);
		}
		else {
			
			return new ResponseEntity<>("{\n" + "\"error\":\"Not authorized\"\n" + "}",HttpStatus.UNAUTHORIZED);
		}		
	}
	
	//Update user information
	@PutMapping(path="/v1/user/self",consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateUser(@RequestHeader(value = "Authorization",required=true) String authToken,
										@Valid @RequestBody(required=true) User user) throws QueriesException{
	
		if(user.getAccount_created() == null && user.getAccount_updated()==null && user.getId()==null) {
			
			User authenticatedUser = checkAuthentication(authToken);
	
			if (authenticatedUser != null) {
				
				LocalDateTime accountUpdated = LocalDateTime.now();
				String newHashPw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
				
				try {
					int updateStatus = userDao.updateUser(user.getFirst_name(), 
														  user.getLast_name(), 
														  newHashPw, 
														  accountUpdated,
														  authenticatedUser.getEmail_address());
					if (updateStatus == 1) {
						return new ResponseEntity<>("{\n" + "\"success\":\"User details Updated\"\n" + "}", HttpStatus.NO_CONTENT);
					}
				}catch (Exception e) {
					throw new QueriesException("Internal SQL Server Error");
				}
			}
				return new ResponseEntity<>("{\n" + "\"error\":\"Username or password is incorrect\"\n" + "}", HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>("{\n" + "\"error\":\"Do not provide account_created,account_updated, and userId fields\"\n" + "}", HttpStatus.BAD_REQUEST);

	}
	
	//Create a user
	@PostMapping(path="/v1/user", consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<?> createUser(@Valid @RequestBody(required=true) User user) throws QueriesException{
		client.incrementCounter("endpoint.createUser.http.post");
		client.incrementCounter("path:/v1/user");
		client.set("uniqueRequest.count", "path:/v1/user");
		logger.trace("This is prerna sharma trace");
		logger.warn("This is prerna sharma warn");
		logger.error("This is error");
		//client.count("saveUser", 2);
		//server.waitForMessage();
		//System.out.println(server.messagesReceived.toString());
		User emailExists = userDao.emailExists(user.getEmail_address());
		
		if(emailExists == null) {
			
			String hashPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			
			user.setPassword(hashPassword);
			user.setAccount_created(LocalDateTime.now());
			user.setAccount_updated(LocalDateTime.now());
			
			try {
				//Save in db
				User userClass = userDao.save(user);
				return new ResponseEntity<>(userClass,HttpStatus.CREATED);
			}catch (Exception e) {
				throw new QueriesException("Internal SQL Server Error");
			}
			
		}
		else
			return new ResponseEntity<>("{\n" + "\"error\":\"User with this email already exist\"\n" + "}",HttpStatus.BAD_REQUEST);
		
	}
	
	public User checkAuthentication(String authToken) throws QueriesException{

		// Remove BASIC from auth-token value
		String[] splitToken = authToken.split("\\s+");
		String token = splitToken[1];

		byte[] tokenBase64 = Base64.getDecoder().decode(token);

		// Get String output of decoded token
		String tokenString = new String(tokenBase64);

		String[] splitStringTok = tokenString.split(":");
		String email_addressToken = splitStringTok[0];
		String passwordToken = splitStringTok[1];
		
		try {
			User usernameExists = userDao.emailExists(email_addressToken);
			
			if(usernameExists != null && BCrypt.checkpw(passwordToken, usernameExists.getPassword())) {
				
				return usernameExists;
		
			}
		}
		catch (Exception e) {
			throw new QueriesException("Internal SQL Server Error");
		}
		return null;
	}
	
}
