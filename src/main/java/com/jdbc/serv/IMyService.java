package com.jdbc.serv;
import java.sql.SQLException;
import java.util.List;

import com.jdbc.dm.Customer;

public interface IMyService {
	
	 String doMultipleQusAndReturn();
	 
     String doMultipleQusAndReturnAgain() throws SQLException;

     List<Customer> insertCustomerBadly(Customer customer);
     
}
