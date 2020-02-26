package com.neu.edu;

import java.sql.SQLException;
import java.time.LocalDate;

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

@RestController
public class FileController {
	
	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private AuthenticationService authService;
	
	@Autowired
	private S3Services s3Service;
	
	@PostMapping(path ="/v1/bill/{id}/file", produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> attachFile(@RequestHeader(value = "Authorization", required=true) String authToken, 
										@PathVariable(required=true)String id,
										@RequestParam ("billAttachment") MultipartFile file) throws QueriesException, FileException, SQLException, ValidationException, IdValidationException, FileTypeException, FileExistsExeption {
		
		User userExists = authService.checkAuthentication(authToken);
		authService.validateFileType(file);
		BillDbEntity billDbEntity = authService.validateBillId(userExists,id);
		authService.FileExists(billDbEntity);
		
		byte[] bytes;
		try{
			bytes = file.getBytes();
			String fileName = id + "_" + file.getOriginalFilename();
			boolean uploaded = s3Service.addFile(bytes, fileName);
			if(uploaded) {
				File fileAttach = new File();
				fileAttach.setFile_name(fileName);
				fileAttach.setUpload_date(LocalDate.now());
				fileAttach.setUrl(System.getenv("BUCKET_NAME") + "/" +  fileName);
				fileAttach.setBillDB(billDbEntity);

				String md5 = DigestUtils.md5DigestAsHex(bytes);
				fileAttach.setFileHash_md5(md5);
				fileAttach.setFileSize_KB((file.getSize()));
				File savedFile = fileDao.save(fileAttach);

				return new ResponseEntity<>(savedFile, HttpStatus.CREATED);
			}
			else {
				throw new FileException("File could not be added into S3");
			}
		}
		catch (Exception e) {
			throw new QueriesException("Internal SQL Server Error");
		}
	}

	
	@GetMapping(path ="/v1/bill/{billId}/file/{fileId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getByFileId(@RequestHeader(value = "Authorization", required=true) String authToken,
										 @PathVariable(required=true)String billId,
										 @PathVariable(required=true)String fileId) throws QueriesException, SQLException, IdValidationException, ValidationException{
		
		User userExists = authService.checkAuthentication(authToken);
		authService.validateBillId(userExists,billId);
		File fileEntityOpt = authService.validateFileId(userExists, billId, fileId);
		
		return new ResponseEntity<>(fileEntityOpt,HttpStatus.OK);
			
	}
	
	@DeleteMapping(path ="/v1/bill/{billId}/file/{fileId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> delAttachmentbyFileId(@RequestHeader(value = "Authorization",required=true) String authToken, 
											     		@PathVariable(required=true)String billId,
											     		@PathVariable(required=true)String fileId) throws FileException, SQLException, ValidationException, IdValidationException, QueriesException{
	
		User userExists = authService.checkAuthentication(authToken);
		authService.validateBillId(userExists,billId);
		File fileEntityOpt = authService.validateFileId(userExists, billId, fileId);
		try {
			String fileName = fileEntityOpt.getFile_name();
			boolean deletedFromS3 = s3Service.deleteFile(fileName);
			if(deletedFromS3) {
				fileDao.deleteById(fileId); // Delete attachment
				return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
			}
			else {
				throw new FileException("File could not be deleted from S3");
			}
		
		}catch (Exception e) {
			throw new QueriesException("Internal SQL Server Error");
		}
	}
	
}
