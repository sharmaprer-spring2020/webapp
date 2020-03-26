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
import com.neu.edu.exception.QueriesException;
import com.neu.edu.pojo.User;
import com.timgroup.statsd.StatsDClient;

@RestController
public class UserController {
	
	private final static Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	UserDao userDao;

	@Autowired
	private StatsDClient statsDClient;
	
	//For health checkpoint
	@GetMapping(path ="/", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> clearhealthCheckpoint() throws QueriesException{
			return new ResponseEntity<>("{\n" + "\"message\":\"To initialize health check\"\n" + "}",HttpStatus.OK);
	}
	
	//Get user Information
	@GetMapping(path ="v1/user/self", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getUserInfo(@RequestHeader(value = "Authorization",required=false) String authToken) throws QueriesException{
		logger.debug("Entered getUser ");
		long start = System.currentTimeMillis();
		statsDClient.incrementCounter("endpoint.v1.user.self.api.get");
		logger.debug("endpoint.v1.user.self.api.get");
		User authenticatedUser = checkAuthentication(authToken);
		if(authenticatedUser != null) {
			long end = System.currentTimeMillis();
			logger.debug("endpoint.v1.user.self.api.get - execution time : " + String.valueOf(end-start));
			statsDClient.recordExecutionTime("endpoint.v1.user.self.api.get", end-start);
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
		logger.debug("Entered put User ");
		long start = System.currentTimeMillis();
		statsDClient.incrementCounter("endpoint.v1.user.self.api.put");
		logger.debug("endpoint.v1.user.self.api.put");
		if(user.getAccount_created() == null && user.getAccount_updated()==null && user.getId()==null) {
			
			User authenticatedUser = checkAuthentication(authToken);
	
			if (authenticatedUser != null) {
				
				LocalDateTime accountUpdated = LocalDateTime.now();
				String newHashPw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
				
				try {
					long dbStart = System.currentTimeMillis();
					int updateStatus = userDao.updateUser(user.getFirst_name(), 
														  user.getLast_name(), 
														  newHashPw, 
														  accountUpdated,
														  authenticatedUser.getEmail_address());
					long dbEnd = System.currentTimeMillis();
					statsDClient.recordExecutionTime("endpoint.v1.user.self.api.db.put", dbEnd-dbStart);
					if (updateStatus == 1) {
						return new ResponseEntity<>("{\n" + "\"success\":\"User details Updated\"\n" + "}", HttpStatus.NO_CONTENT);
					}
				}catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new QueriesException("Internal SQL Server Error");
				}
				finally {
					long end = System.currentTimeMillis();
					logger.debug("endpoint.v1.user.self.api.put - execution time" +String.valueOf(end-start));
					statsDClient.recordExecutionTime("endpoint.v1.user.self.api.put", end-start);
				}
			}
				return new ResponseEntity<>("{\n" + "\"error\":\"Username or password is incorrect\"\n" + "}", HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>("{\n" + "\"error\":\"Do not provide account_created,account_updated, and userId fields\"\n" + "}", HttpStatus.BAD_REQUEST);

	}
	
	//Create a user
	@PostMapping(path="/v1/user", consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<?> createUser(@Valid @RequestBody(required=true) User user) throws QueriesException{
		
		logger.debug("Entered createUser with User : " + user.toString());
		long start = System.currentTimeMillis();
		statsDClient.incrementCounter("endpoint.v1.user.api.post");
		logger.debug("incremented statsDClient for endpoint.v1.user.api.post");
		
		User emailExists = userDao.emailExists(user.getEmail_address());
		
		if(emailExists == null) {
			
			String hashPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			
			user.setPassword(hashPassword);
			user.setAccount_created(LocalDateTime.now());
			user.setAccount_updated(LocalDateTime.now());
			
			try {
				long dbStart = System.currentTimeMillis();
				//Save in db
				User userClass = userDao.save(user);
				long dbEnd = System.currentTimeMillis();
				statsDClient.recordExecutionTime("endpoint.v1.user.api.db.post", dbEnd-dbStart);
				return new ResponseEntity<>(userClass,HttpStatus.CREATED);
			}catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new QueriesException("Internal SQL Server Error");
			}finally {
				long end = System.currentTimeMillis();
				logger.debug("endpoint.v1.user.api.post - execution time : " + String.valueOf(end-start));
				statsDClient.recordExecutionTime("endpoint.v1.user.api.post", end-start);
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
