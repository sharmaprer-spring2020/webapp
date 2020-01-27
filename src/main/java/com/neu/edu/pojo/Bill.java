package com.neu.edu.pojo;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;


public class Bill {
	
	
	@NotNull
	private String vendor;
	@NotNull
	private Date bill_date;
	@NotNull
	private Date due_date;
	@NotNull
	private double amount_due;
	@NotNull
	private List<String> categories;
	@NotNull
	private PaymentStatus paymentStatus;
	
	public Bill() {
		
	}
	
	
	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public Date getBill_date() {
		return bill_date;
	}

	public void setBill_date(Date bill_date) {
		this.bill_date = bill_date;
	}

	public Date getDue_date() {
		return due_date;
	}

	public void setDue_date(Date due_date) {
		this.due_date = due_date;
	}

	public double getAmount_due() {
		return amount_due;
	}

	public void setAmount_due(double amount_due) {
		this.amount_due = amount_due;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	

	
}
