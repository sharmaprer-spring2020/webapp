package com.neu.edu;

import java.time.LocalDateTime;
import java.util.Base64;

import javax.validation.Valid;

import org.mindrot.jbcrypt.BCrypt;
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
import com.neu.edu.pojo.User;


@RestController
public class UserController {
	
	@Autowired
	UserDao userDao;
	
	//Get user Information
	@GetMapping(path ="v1/user/self", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getUserInfo(@RequestHeader(value = "Authorization") String authToken){
		
		User authenticatedUser = checkAuthentication(authToken);
		
		if(authenticatedUser != null) {
			return new ResponseEntity<>(authenticatedUser,HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(null,HttpStatus.UNAUTHORIZED);
		}		
	}
	
	//Update user information
	@PutMapping(path="/v1/user/self",consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateUser(@RequestHeader(value = "Authorization") String authToken,
			@Valid @RequestBody User user) {
	
		if(user.getAccount_created() == null && user.getAccount_updated()==null && user.getId()==null) {
			User authenticatedUser = checkAuthentication(authToken);
	
			if (authenticatedUser != null) {
				LocalDateTime accountUpdated = LocalDateTime.now();
				String newHashPw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
				int updateStatus = userDao.updateUser(user.getFirst_name(), user.getLast_name(), newHashPw, accountUpdated,
						authenticatedUser.getEmail_address());
				if (updateStatus == 1) {
					return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
				}
			}
			
			return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
		}
		
		return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

	}
	
	//Create a user
	@PostMapping(path="/v1/user", consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createUser(@Valid @RequestBody User user){
		
		User emailExists = userDao.emailExists(user.getEmail_address());
		
		if(emailExists == null) {
			
			String hashPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			
			user.setPassword(hashPassword);
			user.setAccount_created(LocalDateTime.now());
			user.setAccount_updated(LocalDateTime.now());
			
			//Save in db
			User userClass = userDao.save(user);
			
			return new ResponseEntity<>(userClass,HttpStatus.CREATED);
		}
		else
		{
			return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
		}
		
	}
	
	public User checkAuthentication(String authToken) {

		// Remove BASIC from auth-token value
		String[] splitToken = authToken.split("\\s+");
		String token = splitToken[1];

		byte[] tokenBase64 = Base64.getDecoder().decode(token);

		// Get String output of decoded token
		String tokenString = new String(tokenBase64);

		String[] splitStringTok = tokenString.split(":");
		String email_addressToken = splitStringTok[0];
		String passwordToken = splitStringTok[1];
				
		User usernameExists = userDao.emailExists(email_addressToken);
		
		if(usernameExists != null && BCrypt.checkpw(passwordToken, usernameExists.getPassword())) {
			
			return usernameExists;
	
		}
		
		return null;
	}
	

}
