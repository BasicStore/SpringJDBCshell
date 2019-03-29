package com.jdbc.serv;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.jdbc.db.IMyDao;
import com.jdbc.dm.Customer;


//@Transactional   // uses default properties
@Transactional(isolation = Isolation.DEFAULT, 
               propagation=Propagation.REQUIRED,
               readOnly=false,  // this is the default anyway - even spring jdbc will not perform non-get operations if this is set to true
               timeout = 30000) // give up on it after 30 seconds
               // noRollbackFor =ArithmeticException.class,
               //value="txManager", // this would link this to a transaction bean
               //rollbackFor = { Exception.class }) // rollback for any exception 

			   //propagation = Propagation.REQUIRES_NEW  ==> means that a transactional method must always create a fresh connection / session
               //                                            regardless of what has come before
               //                                            More applicable to hibernate session

@Service
public class MyService implements IMyService {

	@Autowired
	private IMyDao dao;
	
	
	public String doMultipleQusAndReturn() {
		
		System.out.println(".....about to invoke the ill - fated sequence of dao methods");
		
		dao.insertDummy();
		String output = dao.doSometingBad();
		
		System.out.println(".....completed the dao calls - SHOULD NEVER BE HERE ");
		return output;
	}
	
	

	public String doMultipleQusAndReturnAgain() throws SQLException {
		
		System.out.println(".....about to invoke the ill - fated sequence of dao methods");
		
		dao.insertDummy();
		String output = dao.doSometingBadAgain();
		
		System.out.println(".....completed the dao calls - SHOULD NEVER BE HERE");
		return output;
	}
	
	
	
	public List<Customer> insertCustomerBadly(Customer customer) {
		
		// insert customer
		dao.insert(customer);
		
		return dao.getAllCustomersBadly();
	}
	
	
	
	
}
