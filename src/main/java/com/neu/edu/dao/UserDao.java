package com.neu.edu.dao;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.neu.edu.pojo.User;

@Repository
public interface UserDao extends JpaRepository<User, String> {
	
	@Query(value="Select * FROM user where email_address= :email_address", nativeQuery = true )
	User emailExists(@Param("email_address")String email_address);
	
	@Modifying
	@Transactional
	@Query(value="UPDATE user SET first_name= :newFirstname, last_name= :newLastName, password= :newPassword, account_updated= :newDate WHERE email_address= :email_address", nativeQuery = true )
	int updateUser(@Param("newFirstname")String first_name, 
			@Param("newLastName") String last_name, 
			@Param("newPassword") String password, 
			@Param("newDate") LocalDateTime account_updated,
			@Param("email_address") String email_address);

}
