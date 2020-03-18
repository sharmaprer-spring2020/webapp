package com.neu.edu;

import java.sql.SQLException;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.neu.edu.dao.FileDao;
import com.neu.edu.exception.FileException;
import com.neu.edu.exception.FileExistsExeption;
import com.neu.edu.exception.FileTypeException;
import com.neu.edu.exception.IdValidationException;
import com.neu.edu.exception.QueriesException;
import com.neu.edu.exception.ValidationException;
import com.neu.edu.pojo.BillDbEntity;
import com.neu.edu.pojo.File;
import com.neu.edu.pojo.User;
import com.neu.edu.services.AuthenticationService;
import com.neu.edu.services.S3Services;
import com.timgroup.statsd.StatsDClient;

@RestController
public class FileController {
	
	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private AuthenticationService authService;
	
	@Autowired
	private S3Services s3Service;
	
	@Autowired
	private StatsDClient statsDClient;
	
	private final static Logger logger = LoggerFactory.getLogger(FileController.class);
	
	@PostMapping(path ="/v1/bill/{id}/file", produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> attachFile(@RequestHeader(value = "Authorization", required=true) String authToken, 
										@PathVariable(required=true)String id,
										@RequestParam ("billAttachment") MultipartFile file) throws QueriesException, FileException, SQLException, ValidationException, IdValidationException, FileTypeException, FileExistsExeption {
		logger.debug("Entered attach file ");
		long start = System.currentTimeMillis();
		statsDClient.incrementCounter("endpoint.v1.bill.billId.file.api.post");
		logger.debug("endpoint.v1.bill.billId.file.api.post");
		User userExists = authService.checkAuthentication(authToken);
		authService.validateFileType(file);
		BillDbEntity billDbEntity = authService.validateBillId(userExists,id);
		authService.FileExists(billDbEntity);
		
		byte[] bytes;
		try{
			bytes = file.getBytes();
			String fileName = id + "_" + file.getOriginalFilename();
			long s3Start = System.currentTimeMillis();
			boolean uploaded = s3Service.addFile(bytes, fileName);
			long s3End = System.currentTimeMillis();
			statsDClient.recordExecutionTime("endpoint.v1.bill.billId.file.api.post.s3", s3End-s3Start);
			if(uploaded) {
				File fileAttach = new File();
				fileAttach.setFile_name(fileName);
				fileAttach.setUpload_date(LocalDate.now());
				fileAttach.setUrl(System.getenv("BUCKET_NAME") + "/" +  fileName);
				fileAttach.setBillDB(billDbEntity);

				String md5 = DigestUtils.md5DigestAsHex(bytes);
				fileAttach.setFileHash_md5(md5);
				fileAttach.setFileSize_KB((file.getSize()));
				long dbStart = System.currentTimeMillis();
				File savedFile = fileDao.save(fileAttach);
				long dbEnd = System.currentTimeMillis();
				statsDClient.recordExecutionTime("endpoint.v1.bill.billId.file.api.post.db", dbEnd-dbStart);
				return new ResponseEntity<>(savedFile, HttpStatus.CREATED);
			}
			else {
				throw new FileException("File could not be added into S3");
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new QueriesException("Internal SQL Server Error");
		}finally {
			long end = System.currentTimeMillis();
			logger.debug("endpoint.v1.user.api.post- execution time" +String.valueOf(end-start));			
			statsDClient.recordExecutionTime("endpoint.v1.user.api.post", end-start);
		}
	}

	
	@GetMapping(path ="/v1/bill/{billId}/file/{fileId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getByFileId(@RequestHeader(value = "Authorization", required=true) String authToken,
										 @PathVariable(required=true)String billId,
										 @PathVariable(required=true)String fileId) throws QueriesException, SQLException, IdValidationException, ValidationException{
		logger.debug("Entered get file");
		long start = System.currentTimeMillis();
		statsDClient.incrementCounter("endpoint.v1.bill.billId.file.fileId.api.get");
		logger.debug("incremented endpoint.v1.bill.billId.file.fileId.api.get");
		User userExists = authService.checkAuthentication(authToken);
		authService.validateBillId(userExists,billId);
		File fileEntityOpt = authService.validateFileId(userExists, billId, fileId);
		long end = System.currentTimeMillis();
		logger.debug("endpoint.v1.bill.billId.file.fileId.api.get - execution time" +String.valueOf(end-start));
		statsDClient.recordExecutionTime("endpoint.v1.bill.billId.file.fileId.api.get", end-start);
		return new ResponseEntity<>(fileEntityOpt,HttpStatus.OK);
			
	}
	
	@DeleteMapping(path ="/v1/bill/{billId}/file/{fileId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> delAttachmentbyFileId(@RequestHeader(value = "Authorization",required=true) String authToken, 
											     		@PathVariable(required=true)String billId,
											     		@PathVariable(required=true)String fileId) throws FileException, SQLException, ValidationException, IdValidationException, QueriesException{
		logger.debug("Entered delete attachment");
		long start = System.currentTimeMillis();
		statsDClient.incrementCounter("endpoint.v1.bill.billId.file.fileId.api.delete");
		logger.debug("incremented endpoint.v1.bill.billId.file.fileId.api.delete");
		User userExists = authService.checkAuthentication(authToken);
		authService.validateBillId(userExists,billId);
		File fileEntityOpt = authService.validateFileId(userExists, billId, fileId);
		try {
			String fileName = fileEntityOpt.getFile_name();
			long s3Start = System.currentTimeMillis();
			boolean deletedFromS3 = s3Service.deleteFile(fileName);
			long s3End = System.currentTimeMillis();
			statsDClient.recordExecutionTime("endpoint.v1.bill.billId.file.fileId.api.delete.s3", s3End-s3Start);
			if(deletedFromS3) {
				long dbStart = System.currentTimeMillis();
				fileDao.deleteById(fileId); // Delete attachment
				long dbEnd = System.currentTimeMillis();
				statsDClient.recordExecutionTime("endpoint.v1.bill.billId.file.fileId.api.delete.db", dbEnd-dbStart);
				return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
			}
			else {
				throw new FileException("File could not be deleted from S3");
			}
		
		}catch (Exception e) {
			throw new QueriesException("Internal SQL Server Error");
		}finally {
			long end = System.currentTimeMillis();
			logger.debug("endpoint.v1.bill.billId.file.fileId.api.delete" +String.valueOf(end-start));
			statsDClient.recordExecutionTime("endpoint.v1.bill.billId.file.fileId.api.delete", end-start);
		}
	}
	
}
