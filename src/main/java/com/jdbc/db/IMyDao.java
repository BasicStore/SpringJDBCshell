package com.jdbc.db;

import java.sql.SQLException;
import java.util.List;

import com.jdbc.dm.Customer;

public interface IMyDao {
	
	String doSometingBad();
	
	String doSometingBadAgain() throws SQLException;
	
	List<Customer> getAllCustomersBadly();
	
	List<Customer> getAllCustomersWell();
	
	void insert(Customer customer);
	
	void insertDummy();
}
