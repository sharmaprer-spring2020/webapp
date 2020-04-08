package com.neu.edu;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.edu.dao.BillDao;
import com.neu.edu.dao.FileDao;
import com.neu.edu.dao.UserDao;
import com.neu.edu.exception.FileException;
import com.neu.edu.exception.QueriesException;
import com.neu.edu.pojo.Bill;
import com.neu.edu.pojo.BillDbEntity;
import com.neu.edu.pojo.BillDueRequest;
import com.neu.edu.pojo.User;
import com.neu.edu.services.AWSQueueService;
import com.neu.edu.services.S3Services;
import com.timgroup.statsd.StatsDClient;


@RestController
public class BillController {
	
	@Autowired
	private BillDao billDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private S3Services s3Service;
	
	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private StatsDClient statsDClient;
	
	private static final Logger logger = LoggerFactory.getLogger(BillController.class);
	
	
	@GetMapping(path ="/v1/bills/due/{days}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getBillsByDueDate(@RequestHeader(value = "Authorization", required=true) String authToken,
											   @PathVariable(required=true)int days) throws QueriesException{
		
		logger.debug("Entered get bill by due date");
		long start = System.currentTimeMillis();
		statsDClient.incrementCounter("endpoint.v1.bills.due.api.get");
		logger.debug("incremented endpoint.v1.bills.due.api.get");
		User userExists = checkAuthentication(authToken);
		
		if(userExists != null) {
			
			try {
				System.out.println("Inside bill due");
				System.out.println("Inside bill due with currentdate val: "+LocalDate.now());
			
					BillDueRequest bdr = new BillDueRequest(userExists.getId(), userExists.getEmail_address(), days);
					ObjectMapper objMapper = new ObjectMapper();
					boolean response = AWSQueueService.sendMessage(objMapper.writeValueAsString(bdr));
					//AWSQueueService.readMessage();
					if(!response) {
						return new ResponseEntity<>("{\n" + "\"message\": \"could not process the request\"\n" + "}",HttpStatus.OK);
					}
					return new ResponseEntity<>("{\n" + "\"message\": \"Email will be sent shortly\"\n" + "}",HttpStatus.OK);
			}catch(Exception e) {
				throw new QueriesException("Internal SQL Server Error");
			}finally {
				long end = System.currentTimeMillis();
				logger.debug("endpoint.v1.bills.api.get - Execution time: "+String.valueOf(end-start));
				statsDClient.recordExecutionTime("endpoint.v1.bills.api.get", end-start);
			}
			
		} 
		return new ResponseEntity<>("{\n" + "\"error\": \"Not authenticated\"\n" + "}",HttpStatus.BAD_REQUEST);
		
	 }
	@PostMapping(path ="/v1/bill/", produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createBills(@RequestHeader(value = "Authorization", required=true) String authToken, 
										 @Valid @RequestBody(required=true) Bill bill) throws QueriesException{
		logger.debug("Entered create bill");
		long start = System.currentTimeMillis();
		statsDClient.incrementCounter("endpoint.v1.bill.api.post");		
		logger.debug("incremented endpoint.v1.bill.api.post");
			User userExists = checkAuthentication(authToken);
			
			if(userExists != null) {
				
				BillDbEntity billDb = new BillDbEntity(bill);
				
				billDb.setCreated_ts(LocalDateTime.now());
				billDb.setUpdated_ts(LocalDateTime.now());
				billDb.setOwner_id(userExists.getId());
				
				try {
					long dbStart = System.currentTimeMillis();
					BillDbEntity billDB= billDao.save(billDb);
					long dbEnd = System.currentTimeMillis();
					statsDClient.recordExecutionTime("endpoint.v1.bill.api.post.db", dbEnd-dbStart);
					return new ResponseEntity<>(billDB,HttpStatus.CREATED);
				}catch(Exception e) {
				 throw new QueriesException("Internal SQL Server Error");
			   }finally {
				   long end = System.currentTimeMillis();
				   logger.debug("endpoint.v1.bill.api.post - execution time: "+String.valueOf(end-start));
				   statsDClient.recordExecutionTime("endpoint.v1.bill.api.post", end-start);
			   }
		    }
	   return new ResponseEntity<>("{\n" + "\"error\": \"User not authenticated\"\n" + "}",HttpStatus.BAD_REQUEST);
	}
	
	@GetMapping(path ="/v1/bills", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getAllBills(@RequestHeader(value = "Authorization", required=true) String authToken)throws QueriesException{
		
		logger.debug("Entered get all bills");
		long start = System.currentTimeMillis();
		statsDClient.incrementCounter("endpoint.v1.bills.api.get");
		logger.debug("incremented endpoint.v1.bills.api.get");
		User userExists = checkAuthentication(authToken);
		
		if(userExists != null) {
			
			try {
				List<BillDbEntity> billList = billDao.getBillsByOwnerId(userExists.getId());
				if(billList.isEmpty()) {
					return new ResponseEntity<>("{\n" + "\"message\": \"no bills for this user\"\n" + "}",HttpStatus.OK);
				}
				return new ResponseEntity<>(billList,HttpStatus.OK);
			}catch(Exception e) {
				throw new QueriesException("Internal SQL Server Error");
			}finally {
				long end = System.currentTimeMillis();
				logger.debug("endpoint.v1.bills.api.get - Execution time: "+String.valueOf(end-start));
				statsDClient.recordExecutionTime("endpoint.v1.bills.api.get", end-start);
			}
			
		} 
		return new ResponseEntity<>("{\n" + "\"error\": \"Not authenticated\"\n" + "}",HttpStatus.BAD_REQUEST);
		
	 }
	
	@DeleteMapping(path ="/v1/bill/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> deleteBillbyId(@RequestHeader(value = "Authorization",required=true) String authToken, 
											     @PathVariable(required=true)String id) throws QueriesException{
		logger.debug("Enterered delete bill with id: "+id);
		long start = System.currentTimeMillis();
		statsDClient.incrementCounter("endpoint.v1.bill.billId.api.delete");
		logger.debug("incremented endpoint.v1.bill.billId.api.delete");
		User userExists = checkAuthentication(authToken);
		try {
			Optional<BillDbEntity> billDbEntityOpt = billDao.findById(id);
			if(userExists != null) { //isAuthorisedUser,

				if (billDbEntityOpt.isPresent()) { //Bill id is valid
					
					BillDbEntity billEntity = billDbEntityOpt.get();
					
					if(billEntity.getOwner_id().equals(userExists.getId())) { //Bill has valid owner
						
						billDao.deleteById(id);
						if (billEntity.getAttachment() != null) {
							String fileName = billEntity.getAttachment().getFile_name();
							long s3Start = System.currentTimeMillis();
							boolean deletedFromS3 = s3Service.deleteFile(fileName);
							long s3End = System.currentTimeMillis();
							statsDClient.recordExecutionTime("endpoint.v1.bill.billId.api.delete.s3", s3End-s3Start);
							if(deletedFromS3) {
								long dbStart = System.currentTimeMillis();
								fileDao.deleteById(billEntity.getAttachment().getId()); // Delete attachment
								long dbEnd = System.currentTimeMillis();
								statsDClient.recordExecutionTime("endpoint.v1.bill.billId.api.delete.db", dbEnd-dbStart);
							}
							else {
								throw new FileException("File could not be deleted from S3");
							}
						}
						return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);									
					}
					else
						return new ResponseEntity<>("{\n" + "\"error\":\"Not authorized to delete this bill ID\"\n" + "}",HttpStatus.UNAUTHORIZED);
				}
				else
					return new ResponseEntity<>("{\n" + "\"error\":\"Bill Not Found\"\n" + "}",HttpStatus.NOT_FOUND);
			}
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			throw new QueriesException("Internal SQL Server Error");
		}finally {
			long end = System.currentTimeMillis();
			logger.debug("endpoint.v1.bill.billId.api.delete - Execution time: "+String.valueOf(end-start));
			statsDClient.recordExecutionTime("endpoint.v1.bill.billId.api.delete", end-start);
		}
		return new ResponseEntity<>("{\n" + "\"error\":\"Not authenticated\"\n" + "}",HttpStatus.UNAUTHORIZED);
	}
	
	@GetMapping(path ="/v1/bill/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getBillById(@RequestHeader(value = "Authorization",required=true) String authToken, 
										 @PathVariable(required=true)String id) throws QueriesException{
		logger.debug("Entered get bill with id: "+id);
		long start = System.currentTimeMillis();
		statsDClient.incrementCounter("endpoint.v1.bill.billId.api.get");
		logger.debug("incremented endpoint.v1.bill.billId.api.get");
		User userExists = checkAuthentication(authToken);
		
		try {
			Optional<BillDbEntity> billDbEntityOpt = billDao.findById(id);
			
			if(userExists != null) { //isAuthorisedUser
				
				if (billDbEntityOpt.isPresent()) { //Bill id is valid
					
					BillDbEntity billEntity = billDbEntityOpt.get();
					
					if(billEntity.getOwner_id().equals(userExists.getId())) { //Bill has valid owner
						BillDbEntity billById = billDao.getOne(id);
						return new ResponseEntity<>(billById,HttpStatus.OK);
					}
					else
						return new ResponseEntity<>("{\n" + "\"error\":\"Not authorized to view this bill ID\"\n" + "}",HttpStatus.UNAUTHORIZED);		
				}
				else
					return new ResponseEntity<>("{\n" + "\"error\":\"Bill Not Found\"\n" + "}",HttpStatus.NOT_FOUND);
			}
		}catch (Exception e) {
			throw new QueriesException("Internal SQL Server Error");
		}finally {
			long end = System.currentTimeMillis();
			logger.debug("endpoint.v1.bill.billId.api.get - Execution time: "+String.valueOf(end-start));
			statsDClient.recordExecutionTime("endpoint.v1.bill.billId.api.get", end-start);
		}
	    return new ResponseEntity<>("{\n" + "\"error\":\"Not authenticated\"\n" + "}",HttpStatus.UNAUTHORIZED);
	}
	
	@PutMapping(path ="/v1/bill/{id}", produces=MediaType.APPLICATION_JSON_VALUE,consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> upadateBillbyId(@RequestHeader(value = "Authorization",required=true) String authToken, 
											 @PathVariable(required=true)String id,
											 @Valid @RequestBody(required=true) Bill bill) throws QueriesException{
		logger.debug("Entered put bill for id: "+id);
		long start = System.currentTimeMillis();
		statsDClient.incrementCounter("endpoint.v1.bill.billId.api.put");
		logger.debug("implemented endpoint.v1.bill.billId.api.put");
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
						
						long dbStart = System.currentTimeMillis();
						billEntity = billDao.save(billEntity);
						long dbEnd = System.currentTimeMillis();
						statsDClient.recordExecutionTime("endpoint.v1.bill.billId.api.put.db", dbEnd-dbStart);
						if (billEntity!= null) {
							
							return new ResponseEntity<>(billEntity,HttpStatus.OK);
						}
						
						return new ResponseEntity<>("{\n" + "\"error\":\"Not updated\"\n" + "}",HttpStatus.BAD_REQUEST);
						
					} else
						return new ResponseEntity<>("{\n" + "\"error\":\"Not authorized to view this bill ID\"\n" + "}",HttpStatus.UNAUTHORIZED);
				}
				else
					return new ResponseEntity<>("{\n" + "\"error\":\"Bill Not Found\"\n" + "}",HttpStatus.NOT_FOUND);
			}
		}catch (Exception e) {
			throw new QueriesException("Internal SQL Server Error");	
		}finally {
			long end = System.currentTimeMillis();
			logger.debug("endpoint.v1.bill.billId.api.put - Execution time: "+String.valueOf(end-start));
			statsDClient.recordExecutionTime("endpoint.v1.bill.billId.api.put", end-start);
		}
		return new ResponseEntity<>("{\n" + "\"error\":\"Not authorized\"\n" + "}",HttpStatus.UNAUTHORIZED);
	  }
	  
	  return new ResponseEntity<>("{\n" + "\"error\":\"Do not provide id,created_ts,updated_ts, and owner_id fields\"\n" + "}",HttpStatus.BAD_REQUEST);
			
	 }
	
	 public User checkAuthentication(String authToken) throws QueriesException {

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
