package com.neu.edu;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.neu.edu.dao.BillDao;
import com.neu.edu.dao.UserDao;
import com.neu.edu.pojo.Bill;
import com.neu.edu.pojo.BillDbEntity;
import com.neu.edu.pojo.User;


@RestController
public class BillController {
	
	@Autowired
	private BillDao billDao;
	
	@Autowired
	private UserDao userDao;
	
	
	@PostMapping(path ="/v1/bill/", produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createBills(@RequestHeader(value = "Authorization", required=true) String authToken, 
										 @Valid @RequestBody(required=true) Bill bill){
									
			User userExists = checkAuthentication(authToken);
			
			if(userExists != null) {
				
				BillDbEntity billDb = new BillDbEntity(bill);
				
				billDb.setCreated_ts(LocalDateTime.now());
				billDb.setUpdated_ts(LocalDateTime.now());
				billDb.setOwner_id(userExists.getId());
				
				try {
					BillDbEntity billDB= billDao.save(billDb);
					return new ResponseEntity<>(billDB,HttpStatus.CREATED);
				}
				catch(Exception e) {
					e.printStackTrace();
					
				}
			}
			
	  return new ResponseEntity<>("Check username or password",HttpStatus.BAD_REQUEST);
			
	 }
	
	@GetMapping(path ="/v1/bills", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getAllBills(@RequestHeader(value = "Authorization", required=true) String authToken){
		
		User userExists = checkAuthentication(authToken);
		
		if(userExists != null) {
			
			try {
				List<BillDbEntity> billList = billDao.getBillsByOwnerId(userExists.getId());
				return new ResponseEntity<>(billList,HttpStatus.OK);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
		} 
		return new ResponseEntity<>("Check username or password",HttpStatus.BAD_REQUEST);
	 }
	
	@DeleteMapping(path ="/v1/bill/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> deleteBillbyId(@RequestHeader(value = "Authorization",required=true) String authToken, 
											@PathVariable(required=true)String id){
		
		User userExists = checkAuthentication(authToken);
		try {
			Optional<BillDbEntity> billDbEntityOpt = billDao.findById(id);
			if(userExists != null) { //isAuthorisedUser
				
				//if (billDao.existsById(id)) { //Bill id is valid
				if (billDbEntityOpt.isPresent()) {
					
					BillDbEntity billEntity = billDbEntityOpt.get();
					
					//if(billDao.getInfo(userExists.getId(), id) != null) {
					
					if(billEntity.getOwner_id().equals(userExists.getId())) { //Bill has valid owner
						
						billDao.deleteById(id);
						return new ResponseEntity<>("Bill deleted",HttpStatus.NO_CONTENT);
					}
					else
						return new ResponseEntity<>("Not authorized to delete this bill ID ",HttpStatus.UNAUTHORIZED);
				}
				else
					return new ResponseEntity<>("Bill Not Found",HttpStatus.NOT_FOUND);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		} 
		return new ResponseEntity<>("Username or password Invalid",HttpStatus.UNAUTHORIZED);
	}
	
	@GetMapping(path ="/v1/bill/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getBillById(@RequestHeader(value = "Authorization",required=true) String authToken, 
										 @PathVariable(required=true)String id){
		
		User userExists = checkAuthentication(authToken);
		
		try {
			Optional<BillDbEntity> billDbEntityOpt = billDao.findById(id);
			
			if(userExists != null) { //isAuthorisedUser
				
				//if (billDao.existsById(id)) { 
				if (billDbEntityOpt.isPresent()) { //Bill id is valid
					BillDbEntity billEntity = billDbEntityOpt.get();
					
					//if(billDao.getInfo(userExists.getId(), id) != null) {
					if(billEntity.getOwner_id().equals(userExists.getId())) { //Bill has valid owner
						
						BillDbEntity billById = billDao.getOne(id);
						return new ResponseEntity<>(billById,HttpStatus.OK);
					}
					else
						return new ResponseEntity<>("Not authorized to view this bill ID ",HttpStatus.UNAUTHORIZED);		
				}
				else
					return new ResponseEntity<>("Bill ID Not Found",HttpStatus.NOT_FOUND);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			
		} 
	    return new ResponseEntity<>("Username or password is Incorrect",HttpStatus.UNAUTHORIZED);
	}
	
	@PutMapping(path ="/v1/bill/{id}", produces=MediaType.APPLICATION_JSON_VALUE,consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> upadateBillbyId(@RequestHeader(value = "Authorization",required=true) String authToken, 
											 @PathVariable(required=true)String id,
											 @Valid @RequestBody(required=true) Bill bill){
	 
	  if(bill.getOwner_id() == null && bill.getId()==null && bill.getCreated_ts()==null && bill.getUpdated_ts()==null) { 
		  
		User userExists = checkAuthentication(authToken);
		try {
			Optional<BillDbEntity> billDbEntityOpt = billDao.findById(id);
			
			if(userExists != null) { //isAuthorisedUser
				
				if (billDbEntityOpt.isPresent()) { //Bill id is valid
					
					BillDbEntity billEntity = billDbEntityOpt.get();
					
					if(billEntity.getOwner_id().equals(userExists.getId())) { //Bill has valid owner
						
						
						billEntity.setVendor(bill.getVendor());
						billEntity.setBill_date(bill.getBill_date());
						billEntity.setDue_date(bill.getDue_date());
						billEntity.setAmount_due(bill.getAmount_due());
						billEntity.setCategories(bill.getCategories());
						billEntity.setPaymentStatus(bill.getPaymentStatus());
						billEntity.setUpdated_ts(LocalDateTime.now());
						
						billEntity = billDao.save(billEntity);
						if (billEntity!= null) {
							
							return new ResponseEntity<>(billEntity,HttpStatus.OK);
						}
						
						return new ResponseEntity<>("Not updated",HttpStatus.BAD_REQUEST);
						
					} else
						return new ResponseEntity<>("Not authorized to view this bill ID ",HttpStatus.UNAUTHORIZED);
					
				}
				else
					return new ResponseEntity<>("Bill ID Not Found",HttpStatus.NOT_FOUND);
			}
		}
		catch (Exception e) {
			e.printStackTrace();		} 
		return new ResponseEntity<>("Username or password is Incorrect",HttpStatus.UNAUTHORIZED);
	  }
	  
	  return new ResponseEntity<>("Do not provide id,created_ts,updated_ts, and owner_id fields",HttpStatus.BAD_REQUEST);
			
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
			
			try {
				
				User usernameExists = userDao.emailExists(email_addressToken);
				if(usernameExists != null && BCrypt.checkpw(passwordToken, usernameExists.getPassword())) {
					return usernameExists;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return null;
	 }
		

}
