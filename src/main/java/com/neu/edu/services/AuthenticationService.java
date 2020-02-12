package com.neu.edu.services;

import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.neu.edu.dao.BillDao;
import com.neu.edu.dao.UserDao;
import com.neu.edu.dao.FileDao;
import com.neu.edu.exception.FileExistsExeption;
import com.neu.edu.exception.FileTypeException;
import com.neu.edu.exception.IdValidationException;
import com.neu.edu.exception.ValidationException;
import com.neu.edu.pojo.BillDbEntity;
import com.neu.edu.pojo.File;
import com.neu.edu.pojo.User;

@Component
public class AuthenticationService {

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private BillDao billDao;
	
	@Autowired
	private FileDao fileDao;

	public User checkAuthentication(String authToken) throws SQLException, ValidationException {

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
		if (!(usernameExists != null && BCrypt.checkpw(passwordToken, usernameExists.getPassword()))) {
			//return usernameExists;
			throw new ValidationException("Not authenticated");

		}
		return usernameExists;
	}
	
	public BillDbEntity validateBillId(User user, String billId) throws ValidationException, SQLException, IdValidationException {
		
		if (billId != "" && user != null) {
			
			Optional<BillDbEntity> billDbEntityOpt = billDao.findById(billId);
			if (!billDbEntityOpt.isPresent()){
				throw new IdValidationException("Bill Id Not Found");
			}
			if (!billDbEntityOpt.get().getOwner_id().equals(user.getId())) { //Bill has valid owner
				throw new ValidationException("Not authorized to for this bill ID");
			}else {
				return billDbEntityOpt.get();
			}
		}
		return null;
	}
	
	public File validateFileId(User user,String billId, String fileId) throws IdValidationException,SQLException, ValidationException {
		
		if (fileId != "" && user != null && billId != null) {
			
			Optional<BillDbEntity> billDbEntityOpt = billDao.findById(billId);
			Optional<File> fileEntity = fileDao.findById(fileId);
			
			if (!fileEntity.isPresent()){
				throw new IdValidationException("File Id Not Found");
			}
			if(!fileEntity.get().getBillDB().getId().equals(billDbEntityOpt.get().getId())) {
				throw new ValidationException("Not authorized to delete this billId's attachment");
			} 
			return fileEntity.get();
		}
		return null;
	}
	
	public void validateFileType(MultipartFile file) throws FileTypeException {
		if (!(file.getContentType().equals("image/png") || file.getContentType().equals("application/pdf") || file.getContentType().equals("image/jpeg"))) {
			throw new FileTypeException("Please provide attachment in either of the formats: png,jpeg,jpg or pdf");
		}
				
	}
	
	public void FileExists(BillDbEntity billDb) throws FileExistsExeption {
		if (billDb.getAttachment() != null) {
			throw new FileExistsExeption("Attachment already exists");
		}
				
	}

}
