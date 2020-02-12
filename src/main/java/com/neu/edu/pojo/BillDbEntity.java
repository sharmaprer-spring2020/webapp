package com.neu.edu.pojo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
	@Column(name = "bill_id", columnDefinition = "VARCHAR(36)")
	private String id;
	
	@Column
	@ReadOnlyProperty
	private LocalDateTime created_ts;
	
	@Column
	@ReadOnlyProperty
	private LocalDateTime updated_ts;
	
	@Column(columnDefinition ="VARCHAR(36)")
	@NotNull
	@ReadOnlyProperty
	private String owner_id;
	
	@Column
	@NotNull
	@NotBlank
	private String vendor;
	
	@Column
	@NotNull
	//@Temporal(TemporalType.DATE)
	private LocalDate bill_date;
	
	@Column
	@NotNull
	//@Temporal(TemporalType.DATE)
	private LocalDate due_date;
	
	@Column
	@NotNull
	private Double amount_due;
	
	@Column
	@NotNull
	@ElementCollection
	@Size(min=1)
	private Set<String> categories;
	
	@Column
	@NotNull
	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus;
	
	@OneToOne(fetch=FetchType.LAZY, mappedBy="billDB")
	private File attachment;

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

	public double getAmount_due() {
		return amount_due;
	}

	public void setAmount_due(double amount_due) {
		this.amount_due = amount_due;
	}

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
	
	
	public File getAttachment() {
		return attachment;
	}

	public void setAttachment(File attachment) {
		this.attachment = attachment;
	}

	public BillDbEntity() {
		
	}
	
	public BillDbEntity(Bill bill) {
		super();
		this.setVendor(bill.getVendor());
		this.setBill_date(bill.getBill_date());
		this.setDue_date(bill.getDue_date());
		this.setAmount_due(bill.getAmount_due());
		this.setCategories(bill.getCategories());
		this.setPaymentStatus(bill.getPaymentStatus());
		
	}
	
}
