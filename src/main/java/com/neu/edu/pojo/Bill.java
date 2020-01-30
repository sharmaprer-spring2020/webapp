package com.neu.edu.pojo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Bill {
	
	private String id;
	private LocalDateTime created_ts;
	private LocalDateTime updated_ts;
	private String owner_id;
	
	@NotNull
	@NotBlank
	private String vendor;
	
	@NotNull
	private LocalDate bill_date;
	
	@NotNull
	private LocalDate due_date;
	
	@NotNull
	private Double amount_due;
	
	@NotNull
	private Set<String> categories;
	
	@NotNull
	private PaymentStatus paymentStatus;

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	@JsonFormat(pattern="yyyy-MM-dd")
	public LocalDate getBill_date() {
		return bill_date;
	}

	public void setBill_date(LocalDate bill_date) {
		this.bill_date = bill_date;
	}

	@JsonFormat(pattern="yyyy-MM-dd")
	public LocalDate getDue_date() {
		return due_date;
	}

	public void setDue_date(LocalDate due_date) {
		this.due_date = due_date;
	}
	
	@DecimalMin(value="0.01")
	public Double getAmount_due() {
		return amount_due;
	}

	public void setAmount_due(Double amount_due) {
		this.amount_due = amount_due;
	}
	
	@ElementCollection
	@Size(min=1)
	public Set<String> getCategories() {
		return categories;
	}

	public void setCategories(Set<String> categories) {
		this.categories = categories;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	
	public String getId() {
		return id;
	}

	public LocalDateTime getCreated_ts() {
		return created_ts;
	}


	public LocalDateTime getUpdated_ts() {
		return updated_ts;
	}

	public String getOwner_id() {
		return owner_id;
	}
	

}
