package com.neu.edu.pojo;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.ReadOnlyProperty;

import com.fasterxml.jackson.annotation.JsonProperty;

@Table(name = "file")
@Entity
public class File{
	
	@Column
	@NotNull
	@ReadOnlyProperty
	private String file_name;
	
	@Id
	@ReadOnlyProperty
	@GeneratedValue(generator = "UUID2")
	@GenericGenerator( name="UUID2", strategy = "uuid2")
	@Column(name = "id", columnDefinition = "VARCHAR(36)")
	private String id;
	
	@Column
	@NotNull
	@ReadOnlyProperty
	private String url;
	
	@Column
	@NotNull
	@ReadOnlyProperty
	private LocalDate upload_date;
	
	@JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	@OneToOne
	@JoinColumn(name= "bill_id", nullable= false)
	private BillDbEntity billDB;
	
	@Column
	@JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	private String fileHash_md5;
	
	@Column
	@JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	private long fileSize_KB;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFile_name() {
		return file_name;
	}

	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public LocalDate getUpload_date() {
		return upload_date;
	}

	public void setUpload_date(LocalDate upload_date) {
		this.upload_date = upload_date;
	}

	public BillDbEntity getBillDB() {
		return billDB;
	}

	public void setBillDB(BillDbEntity billDbEntityOpt) {
		this.billDB = billDbEntityOpt;
	}

	public String getFileHash_md5() {
		return fileHash_md5;
	}

	public void setFileHash_md5(String fileHash_md5) {
		this.fileHash_md5 = fileHash_md5;
	}

	public long getFileSize_KB() {
		return fileSize_KB;
	}

	public void setFileSize_KB(long fileSize_KB) {
		this.fileSize_KB = fileSize_KB;
	}

	public File(String file_name,String url, LocalDate upload_date, String id) {
		
		this.file_name = file_name;
		this.url = url;
		this.upload_date = upload_date;
		this.id = id;
	}
	
	public File() {
		
	}

	
}
