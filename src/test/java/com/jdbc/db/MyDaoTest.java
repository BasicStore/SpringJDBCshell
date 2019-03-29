package com.jdbc.db;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.jdbc.dm.Customer;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MyDaoTest {
	
	@Autowired
	public IMyDao dao;
	
	
	@Test
	public void testGetAllCustomersWell() {

		assertTrue(dao != null);
		
		String firstname="";
		String lastname="";
		dao.insert(new Customer(firstname, lastname));
		
		// get all customers (includes those added in main app run() method)
		List<Customer> allCustomers = dao.getAllCustomersWell();

		boolean actuallyPresent = allCustomers.parallelStream().filter(
				c->c.getFirstName().equals(firstname) && c.getLastName().equals(lastname)).findFirst().isPresent();
		assertTrue(actuallyPresent);
	}
	
	
}
