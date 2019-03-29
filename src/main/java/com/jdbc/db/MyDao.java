package com.jdbc.db;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.jdbc.SpringJdbcOnlyApplication;
import com.jdbc.dm.Customer;


@Repository
public class MyDao implements IMyDao {
	
	private static final Logger log = LoggerFactory.getLogger(MyDao.class);
	
	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	
	public String doSometingBad() {
		throw new NullPointerException("all went wrong on db call");
	}	
	
	
	public String doSometingBadAgain() throws SQLException {
		throw new SQLException("all went wrong on this db call, throwing a checked exception");
	}
	
	
	public void insert(Customer customer) {
		jdbcTemplate.update("INSERT INTO customers(first_name, last_name) VALUES (?,?)", 
				customer.getFirstName(), customer.getLastName());
		log.info("customer inserted");
	}
	
	
	public void insertDummy() {
		log.info("pretned to insert a new customer - but don't really");
	}
	
	
	
	public List<Customer> getAllCustomersBadly() {
    	// get the contents of the customers table
        log.info("Querying all customer records");
        return jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers", 
                (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")) // mapRow(ResultSet rs, int rowNum)
        );
    }
	
	
	public List<Customer> getAllCustomersWell() {
		return jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers", 
                (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")) // mapRow(ResultSet rs, int rowNum)
        );
	}
	
	
}
