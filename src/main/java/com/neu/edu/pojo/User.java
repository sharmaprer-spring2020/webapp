package com.neu.edu.pojo;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.ReadOnlyProperty;

import com.fasterxml.jackson.annotation.JsonProperty;


@Table(name = "user")
@Entity
public class User {
	
	@Id
	//@ReadOnlyProperty
	@ReadOnlyProperty
	@GeneratedValue(generator = "UUID2")
	@GenericGenerator( name="UUID2", strategy = "uuid2")
	@Column(name = "user_id", updatable = false, columnDefinition = "VARCHAR(36)",insertable = false)
	private String id;
	
	@NotBlank(message ="First name value cannot be empty")
	@NotNull
	@Column
	private String first_name;
	
	@NotBlank(message ="Last name value cannot be empty")
	@NotNull
	@Column
	private String last_name;
	
	@NotBlank(message ="Password cannot be empty")
	@NotNull
	@JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	@Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d])(?=.*[!@#$%^&/*><]).{8,64}$",message="Please provide password of length 8-64 with one special character(!,@,#,$,%,^,&,*,>,<, and /),1 lowercase, 1 uppercase, 1 digit")
	@Column
	private String password;
	
	@NotBlank(message ="Email value cannot be empty")
	@Email
	@NotNull
	@Pattern(regexp="^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",message ="Please Provide email address of the form p.s@example.com or p.s@ex.am.com")
	@Column
	private String email_address;
	
	@ReadOnlyProperty
	@Column
	private LocalDateTime account_created;
	
	@ReadOnlyProperty
	@Column
	private LocalDateTime account_updated;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name="owner_id")
	@JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	private Set<BillDbEntity> bill;

	public User() {
		
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail_address() {
		return email_address;
	}

	public void setEmail_address(String email_address) {
		this.email_address = email_address;
	}

	public LocalDateTime getAccount_created() {
		return account_created;
	}

	public void setAccount_created(LocalDateTime account_created) {
		this.account_created = account_created;
	}

	public LocalDateTime getAccount_updated() {
		return account_updated;
	}

	public void setAccount_updated(LocalDateTime account_updated) {
		this.account_updated = account_updated;
	}

	public Set<BillDbEntity> getBill() {
		return bill;
	}
	public void setBill(Set<BillDbEntity> bill) {
		this.bill = bill;
	}
	@Override
	public String toString() {
		return "User [id=" + id + ", first_name=" + first_name + ", last_name=" + last_name + ", password=" + password
				+ ", email_address=" + email_address + ", account_created=" + account_created + ", account_updated="
				+ account_updated + "]";
	}
	

}
