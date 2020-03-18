package com.neu.edu;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.neu.edu.dao.UserDao;
import com.neu.edu.pojo.User;
import com.timgroup.statsd.StatsDClient;

@WebMvcTest(UserController.class)
class Assignment2ApplicationTests {

	@Autowired
	private MockMvc mock;
	
	@MockBean
	private UserDao userdao;
	
	@MockBean
	private StatsDClient statsDClient;
	
	private String userObj = "{\n" + 
			"    \"first_name\": \"Prerna\",\n" + 
			"    \"last_name\": \"Sharma\",\n" + 
			"    \"password\": \"U@snov02\",\n" + 
			"    \"email_address\":\"prena@gmail.com\"\n" + 
			"}"; 
	
	@Test
	void ifUsernameAlreadyExist() throws Exception {
		
		when(userdao.emailExists(ArgumentMatchers.anyString())).thenReturn(new User());
		
		mock.perform(post("/v1/user")
							.content(userObj)
							.contentType(MediaType.APPLICATION_JSON))
							.andDo(print()).andExpect(status().isBadRequest());
	}
	
	@Test
	void checkUserSaved() throws Exception{
		
		User user = new User();
		
		user.setId("qwer");
		user.setFirst_name("Prerna");
		user.setLast_name("sharma");
		user.setEmail_address("prerna");
		user.setPassword("U");
	
		
		when(userdao.emailExists(ArgumentMatchers.anyString())).thenReturn(null);
		
		when(userdao.save(ArgumentMatchers.any())).thenReturn(user);
		
		mock.perform(post("/v1/user")
							.content(userObj)
							.contentType(MediaType.APPLICATION_JSON))
							.andDo(print())
							.andExpect(status().isCreated());
	}
	

}
