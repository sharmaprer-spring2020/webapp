package com.neu.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.neu.edu.pojo.File;

public interface FileDao extends JpaRepository<File, String>{

}
