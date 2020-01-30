package com.neu.edu.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.neu.edu.pojo.BillDbEntity;

@Repository
public interface BillDao extends JpaRepository<BillDbEntity, String> {
	
	
	@Query(value="Select * FROM bill where owner_id= :owner_id", nativeQuery = true )
	List<BillDbEntity> getBillsByOwnerId(@Param("owner_id")String owner_id);
	
	@Query(value="Select * FROM bill where owner_id= :owner_id AND id= :id", nativeQuery = true )
	BillDbEntity getInfo(@Param("owner_id")String owner_id,
						 @Param("id")String id);

}
