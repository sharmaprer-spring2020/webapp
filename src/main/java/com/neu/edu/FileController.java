package com.neu.edu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
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

import com.neu.edu.dao.BillDao;
import com.neu.edu.dao.FileDao;
import com.neu.edu.dao.UserDao;
import com.neu.edu.exception.FileException;
import com.neu.edu.exception.QueriesException;
import com.neu.edu.pojo.BillDbEntity;
import com.neu.edu.pojo.File;
import com.neu.edu.pojo.User;

@RestController
public class FileController {
	
	@Autowired
	private BillDao billDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private FileDao fileDao;
		
	@PostMapping(path ="/v1/bill/{id}/file", produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> attachFile(@RequestHeader(value = "Authorization", required=true) String authToken, 
										 @PathVariable(required=true)String id,
										 @RequestParam ("billAttachment") MultipartFile file) throws QueriesException, FileException {
		
		User userExists = checkAuthentication(authToken);
		Optional<BillDbEntity> billDbEntity = billDao.findById(id);

		if((file.getContentType().equals("image/png")) || file.getContentType().equals("application/pdf") || file.getContentType().equals("image/jpeg")) { //if valid file format
			
			if(userExists != null) {//isAuthorisedUser
			
				if(billDbEntity.isPresent()) {//is a valid Bill Id
					if (billDbEntity.get().getAttachment() == null) { //Attachment already exists
						
						try {
							String localpath = createDir();
							byte[] bytes;
							try{
								bytes = file.getBytes();
								
								Path pathDir = Paths.get(localpath+"/"+id + "_" + file.getOriginalFilename());
									
								Files.write(pathDir, bytes);
								
								File fileAttach = new File();
								fileAttach.setFile_name(file.getOriginalFilename());
								fileAttach.setUpload_date(LocalDate.now());
								fileAttach.setUrl(pathDir.toString());
								fileAttach.setBillDB(billDbEntity.get());
								
								String md5 = DigestUtils.md5DigestAsHex(bytes);
								fileAttach.setFileHash(md5);
								fileAttach.setFileSize_KB((file.getSize()));
								System.out.println("md5 value:" +md5);
								System.out.println("file size value:" +file.getSize());
								File savedFile = fileDao.save(fileAttach);
								
								return new ResponseEntity<>(savedFile,HttpStatus.CREATED);
										
							 }catch (IOException e1) {
									throw new QueriesException("Internal File IO exception");
							 }catch (Exception e) {
									throw new QueriesException("Internal SQL Server Error");
							 }
						} catch (Exception e2) {
							throw new FileException("Directory not found");
						}
				   } 
					 return new ResponseEntity<>("{\n" + "\"error\": \"bill already exists delete first to add new\"\n" + "}",HttpStatus.BAD_REQUEST);
				 }
				return new ResponseEntity<>("{\n" + "\"error\":\"Bill Id Not Found\"\n" + "}",HttpStatus.NOT_FOUND);
			
			}
			return new ResponseEntity<>("{\n" + "\"error\": \"User does not exist\"\n" + "}",HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("{\n" + "\"error\": \"Please provide attachment in either of the formats: png,jpeg,jpg or pdf\"\n" + "}",HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}
	
	@GetMapping(path ="/v1/bill/{billId}/file/{fileId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getAttachmentByFileId(@RequestHeader(value = "Authorization", required=true) String authToken,
										 @PathVariable(required=true)String billId,
										 @PathVariable(required=true)String fileId) throws QueriesException{
		User userExists = checkAuthentication(authToken);
		Optional<BillDbEntity> billDbEntityOpt = billDao.findById(billId);
		Optional<File> fileEntity = fileDao.findById(fileId);
		
			if(userExists != null) { //isAuthorisedUser
				if (billDbEntityOpt.isPresent()) { //Bill id is valid
					if(fileEntity.isPresent()) { //file Id is valid
						if(fileEntity.get().getBillDB().getId().equals(billDbEntityOpt.get().getId())) {
							return new ResponseEntity<>(fileEntity.get(),HttpStatus.OK);
						}
						return new ResponseEntity<>("{\n" + "\"error\":\"Cannot view other bills attachement\"\n" + "}",HttpStatus.BAD_REQUEST);
					}
					return new ResponseEntity<>("{\n" + "\"error\":\"File ID Not Found\"\n" + "}",HttpStatus.NOT_FOUND);			
				}
				else
					return new ResponseEntity<>("{\n" + "\"error\":\"Bill ID Not Found\"\n" + "}",HttpStatus.NOT_FOUND);
			}
		
		return new ResponseEntity<>("{\n" + "\"error\": \"Check username or password\"\n" + "}",HttpStatus.BAD_REQUEST);
	}
	
	@DeleteMapping(path ="/v1/bill/{billId}/file/{fileId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> deleteAttachmentbyFileId(@RequestHeader(value = "Authorization",required=true) String authToken, 
											     		   @PathVariable(required=true)String billId,
											     		   @PathVariable(required=true)String fileId) throws QueriesException, FileException{
		
		User userExists = checkAuthentication(authToken);
		Optional<BillDbEntity> billDbEntityOpt = billDao.findById(billId);
		Optional<File> fileEntity = fileDao.findById(fileId);
		
		if(userExists != null) { //isAuthorisedUser
			if(billDbEntityOpt.isPresent()) { //Bill id is valid
				if(fileEntity.isPresent()) { //file Id is valid
					if(fileEntity.get().getBillDB().getId().equals(billDbEntityOpt.get().getId())) { //File has valid bill owner
						
						try{
						fileEntity.get().getBillDB().setAttachment(null);
						String FilePath= fileEntity.get().getUrl();
						Path pathDir = Paths.get(FilePath);
						Files.deleteIfExists(pathDir);
						fileDao.deleteById(fileId); //Delete attachment
						return new ResponseEntity<>(null,HttpStatus.NO_CONTENT);
						
						}catch (IOException e1) {
								throw new FileException("Internal File IO exception");
						 }catch (Exception e) {
								throw new QueriesException("Internal SQL Server Error");
						 }
					}
					return new ResponseEntity<>("{\n" + "\"error\":\"Cannot delete other bills attachement\"\n" + "}",HttpStatus.UNAUTHORIZED);
				}
				return new ResponseEntity<>("{\n" + "\"error\":\"File ID Not Found\"\n" + "}",HttpStatus.NOT_FOUND);			
			}
			return new ResponseEntity<>("{\n" + "\"error\":\"Bill ID Not Found\"\n" + "}",HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>("{\n" + "\"error\": \"Check username or password\"\n" + "}",HttpStatus.BAD_REQUEST);
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
