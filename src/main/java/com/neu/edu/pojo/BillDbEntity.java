package com.neu.edu.pojo;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.ReadOnlyProperty;

import com.fasterxml.jackson.annotation.JsonFormat;

@Table(name = "bill")
@Entity
public class BillDbEntity {
	
	@Id
	@ReadOnlyProperty
	@GeneratedValue(generator = "UUID2")
	@GenericGenerator( name="UUID2", strategy = "uuid2")
	@Column(name = "id", columnDefinition = "VARCHAR(36)")
	private String id;
	
	@Column
	@ReadOnlyProperty
	private LocalDateTime created_ts;
	
	@Column
	@ReadOnlyProperty
	private LocalDateTime updated_ts;
	
	@Column(columnDefinition ="VARCHAR(36)")
	@NotNull
	private String owner_id;
	
	@Column
	@NotNull
	private String vendor;
	
	@Column
	@NotNull
	@Temporal(TemporalType.DATE)
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date bill_date;
	
	@Column
	@Temporal(TemporalType.DATE)
	@JsonFormat(pattern="yyyy-MM-dd")
	@NotNull
	private Date due_date;
	
	@Column
	@NotNull
	private double amount_due;
	
	@Column
	@NotNull
	@ElementCollection
	private List<String> categories;
	
	@Column
	@NotNull
	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LocalDateTime getCreated_ts() {
		return created_ts;
	}

	public void setCreated_ts(LocalDateTime created_ts) {
		this.created_ts = created_ts;
	}

	public LocalDateTime getUpdated_ts() {
		return updated_ts;
	}

	public void setUpdated_ts(LocalDateTime updated_ts) {
		this.updated_ts = updated_ts;
	}


	public String getOwner_id() {
		return owner_id;
	}

	public void setOwner_id(String owner_id) {
		this.owner_id = owner_id;
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
	
	public BillDbEntity() {
		
	}
	
	public BillDbEntity(Bill bill) {
		
		this.setCreated_ts(LocalDateTime.now());
		this.setUpdated_ts(LocalDateTime.now());
		this.setVendor(bill.getVendor());
		this.setAmount_due(bill.getAmount_due());
		this.setBill_date(bill.getBill_date());
		this.setCategories(bill.getCategories());
		this.setPaymentStatus(bill.getPaymentStatus());
		this.setDue_date(bill.getDue_date());
		
		
	}

	
}
