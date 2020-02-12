package com.neu.edu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

@RestController
public class FileController {
	
	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private AuthenticationService authService;
	
	@PostMapping(path ="/v1/bill/{id}/file", produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> attachFile(@RequestHeader(value = "Authorization", required=true) String authToken, 
										@PathVariable(required=true)String id,
										@RequestParam ("billAttachment") MultipartFile file) throws QueriesException, FileException, SQLException, ValidationException, IdValidationException, FileTypeException, FileExistsExeption {
		
		User userExists = authService.checkAuthentication(authToken);
		authService.validateFileType(file);
		BillDbEntity billDbEntity = authService.validateBillId(userExists,id);
		authService.FileExists(billDbEntity);
		String localpath = createDir();
		byte[] bytes;
		try{
			bytes = file.getBytes();

			Path pathDir = Paths.get(localpath + "/" + id + "_" + file.getOriginalFilename());

			Files.write(pathDir, bytes);

			File fileAttach = new File();
			fileAttach.setFile_name(file.getOriginalFilename());
			fileAttach.setUpload_date(LocalDate.now());
			fileAttach.setUrl(pathDir.toString());
			fileAttach.setBillDB(billDbEntity);

			String md5 = DigestUtils.md5DigestAsHex(bytes);
			fileAttach.setFileHash_md5(md5);
			fileAttach.setFileSize_KB((file.getSize()));
			File savedFile = fileDao.save(fileAttach);

			return new ResponseEntity<>(savedFile, HttpStatus.CREATED);
									
			}catch (IOException e1) {
				throw new QueriesException("Internal File IO exception");
			}catch (Exception e) {
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
				String filePath = fileEntityOpt.getUrl();
				Path pathDir = Paths.get(filePath);
				Files.deleteIfExists(pathDir); //Remove file from local
				fileDao.deleteById(fileId); // Delete attachment
				
				return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
				
			}catch (IOException e1) {
				throw new FileException("Internal File IO exception");
			}catch (Exception e) {
				throw new QueriesException("Internal SQL Server Error");
			}
	}
	
	public String createDir() throws FileException {

		Path dirLoc = Paths.get(Paths.get("./temp")+"/");
		boolean dirExists = Files.exists(dirLoc);
		if(!dirExists) {
			try {
				Files.createDirectories(dirLoc);
			}catch (IOException e) {
				throw new FileException("Directory not found");
			}
		}
		return dirLoc.toString();
		
	}
}
