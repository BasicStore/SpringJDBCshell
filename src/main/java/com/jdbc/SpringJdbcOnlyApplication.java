package com.jdbc;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import com.jdbc.dm.Customer;
import com.jdbc.post.MailSender;
import com.jdbc.serv.IMyService;


@SpringBootApplication
public class SpringJdbcOnlyApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringJdbcOnlyApplication.class);

	// EXAMPLES:  
	// https://www.concretepage.com/spring-boot/spring-boot-jdbc-example
	// https://www.baeldung.com/spring-jdbc-jdbctemplate
	
	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private IMyService myService;
	
	@Autowired
	private MailSender mailSender;
	
	public static void main(String[] args) {
		SpringApplication.run(SpringJdbcOnlyApplication.class, args);
	}

	
    @Override
    public void run(String... strings) throws Exception {
    	log.info((mailSender == null) ? "mailsender is NULL" : "mailsender is GOOD");
    	
    	mailSender.sendEmail("Attempt to send an email with spring mail....");
    	
    	
    	log.info("Creating tables");

        //jdbcTemplate.execute("create database springjdbc");
        jdbcTemplate.execute("DROP database IF EXISTS springjdbc");   // H2= DROP database springjdbc IF EXISTS 
        jdbcTemplate.execute("create database springjdbc");
        jdbcTemplate.execute("use springjdbc");
        jdbcTemplate.execute("DROP TABLE IF EXISTS customers");  // H2= DROP TABLE customers IF EXISTS   
        jdbcTemplate.execute("CREATE TABLE customers("+
                "id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");
                
        // Split up the array of whole names into an array of first/last names
        List<Object[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long").stream()
                .map(name -> name.split(" "))
                .collect(Collectors.toList());

        // Use a Java 8 stream to print out each tuple of the list
        splitUpNames.forEach(name -> log.info(String.format("Inserting customer record for %s %s", name[0], name[1])));

        // Uses JdbcTemplate's batchUpdate operation to bulk load data
        jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?,?)", splitUpNames);

        printCustomers("Josh");
        
        // insert a couple of single rows
        jdbcTemplate.update("INSERT INTO customers(first_name, last_name) VALUES (?,?)", new String[] {"Quilio", "Quart"});
        jdbcTemplate.update("INSERT INTO customers(first_name, last_name) VALUES (?,?)", "Quilio2", "Quart2");
        
        log.info("Executing update: Woo becomes changeable ");
        String SQL = "update customers set first_name = ? where last_name = ?";
        jdbcTemplate.update(SQL, "Changeable", "Woo");

        printAllCustomers();
        
        deleteCustomer("Quart2");
        printAllCustomers();
        deleteCustomers("Josh");
        printAllCustomers();

        // this can return an empty list without issue if that is necessary 
        List<Customer> myCustomers = getAllCustomers();
        log.info("number of customers = " + myCustomers.size());
        
        Customer customer = getCustomer("Woodle");
        log.info(customer != null ? "Customer retrieved = " + customer.getFirstName() + " " + customer.getLastName() : "NO CUSTOMER FOUND");
        customer = getCustomer("Woo");
        log.info(customer != null ? "Customer retrieved = " + customer.getFirstName() + " " + customer.getLastName() : "NO CUSTOMER FOUND");
        insertBatchCustomers(Arrays.asList(new Customer("Mark", "Smith"), new Customer("Mark2", "Smith2"), new Customer("Mark3", "Smith3")));   
        printAllCustomers();
        
        // artificially test transactional annotations
        testTransactionalSpringJdbcWithoutJPA();
        
    }
    
    
    private void testTransactionalSpringJdbcWithoutJPA() {
    	
    	List<Customer> customerList = getAllCustomers(); 
    	log.info("Number of customers BEFORE ENTRY INTO TRANSACTION SCOPE  = " + customerList.size());
    	
    	log.info("START TRANSACTIONS ***************************************************************************************");
    	try { // catch an unchecked exception
        	myService.doMultipleQusAndReturn();
        } catch(Exception e) {
        	log.info("UNCHECKED EXCEPTION CAUGHT ==> " + e.getMessage());
        }
    	
    	try { // catch a checked excetion
        	myService.doMultipleQusAndReturnAgain();
        } catch(Exception e) {
        	log.info("CHECKED EXCEPTION CAUGHT ==> " + e.getMessage());
        }
    	
    	
        try
        {
        	List<Customer> updatedCustomers = myService.insertCustomerBadly(new Customer("Boo", "Radley")); 
        	log.info("Number of customers after insertion = " + updatedCustomers.size());
        }
        catch (InvalidResultSetAccessException e) 
        {
        	log.info("caught InvalidResultSetAccessException");
        	//throw new RuntimeException(e);
        } 
        catch (DataAccessException e) // seems to work for SQL issues tpoo
        {
        	log.info("caught DataAccessException=" + e.getMessage());
        	//throw new RuntimeException(e);
        }
        catch(Exception e) {
        	log.info("caught Exception = " + e.getMessage());
        }
        
    	log.info("END TRANSACTIONS ***************************************************************************************");
    	
    	customerList = getAllCustomers(); 
    	log.info("Number of customers AFTER EXITING TRANSACTION SCOPE, after rollback if appropriate hopefully = " + customerList.size());
    }
    
        
    
    protected void insertBatchCustomers(List<Customer> customerList) {
    	List<Object[]> names = customerList.stream()
                .map(name -> new String[] {name.getFirstName(), name.getLastName()})
                .collect(Collectors.toList());
    	jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?,?)", names);
    	
    }
    
   
    
    protected void printAllCustomers() {
    	// get the contents of the customers table
        log.info("Querying all customer records");
        jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers ", 
                (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")) // mapRow(ResultSet rs, int rowNum)
        ).forEach(customer -> log.info("--> Any Customer: " + customer.toString()));
    }
   
    
    
    protected void printCustomers(String firstName) {
    	log.info("Querying for customer records where first_name = '"+firstName+ "':");
        jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers WHERE first_name = ?", new Object[] { firstName },
                (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
        ).forEach(customer -> log.info("--> " + customer.toString()));
        
    }
    
	
    // obviously would not catch this here in a real app
    protected Customer getCustomer(String lastName) {
    	try {
    		return jdbcTemplate.queryForObject(
                    "SELECT id, first_name, last_name FROM customers WHERE last_name = ? LIMIT 1", new Object[] { lastName }, 
                    (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
            );
    	} catch(EmptyResultDataAccessException e) {
    		log.info("No data found = " + e.getClass().getName() + e.getMessage());
    		return null;
    	}
    }
       
    
    protected List<Customer> getAllCustomers() {
    	// get the contents of the customers table
        log.info("Querying all customer records");
        return jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers", 
                (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")) // mapRow(ResultSet rs, int rowNum)
        );
    }
    
    
    protected void deleteCustomer(String lastName) {
    	String sql = "DELETE FROM customers WHERE last_name = ?";
    	jdbcTemplate.update(sql, lastName);
    	log.info("Customer deleted: " + lastName);
    } 
    
    
    protected void deleteCustomers(String firstName) {
    	String sql = "DELETE FROM customers WHERE first_name = ?";
    	jdbcTemplate.update(sql, firstName);
    	log.info("Customers deleted with first name = " + firstName);
    }
    
    
    
    
   
    
    
    
    // LAMDA MAPPER:  JdbcTemplate.query(SQL, args for sql, rowMapper->mapping result set to object)
    //                    -->   List<T> query(java.lang.String sql, java.lang.Object[] args, RowMapper<T> rowMapper)

    /* MAPPER EXAMPLE    // mapRow(ResultSet rs, int rowNum)
     * 
     * 
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import org.springframework.jdbc.core.RowMapper;

    public class StudentMapper implements RowMapper<Student> {
       public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
          Customer c = new Customer();
          
          
          Student student = new Student();
          student.setId(rs.getInt("id"));
          student.setName(rs.getString("name"));
          student.setAge(rs.getInt("age"));
          return student;
       }
    }
    */
    
	
}
