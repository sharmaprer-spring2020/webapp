package com.neu.edu.dao;

import java.time.LocalDate;
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
	
	@Query(value="Select * FROM bill where owner_id= :owner_id AND bill_id= :bill_id", nativeQuery = true )
	BillDbEntity getInfo(@Param("owner_id")String owner_id,
						 @Param("bill_id")String id);
	
	@Query(value="Select * FROM bill where owner_id= :owner_id AND due_date < :date "
			+ "AND payment_status='due'"
			+ "AND due_date >= :currentDate", nativeQuery = true)
			//+ "AND due_date >= CURDATE()", nativeQuery = true)
	List<BillDbEntity> getByDueDate(@Param("owner_id")String owner_id,
			 						@Param("date")LocalDate date,
			 						@Param ("currentDate") LocalDate currentDate);

}
