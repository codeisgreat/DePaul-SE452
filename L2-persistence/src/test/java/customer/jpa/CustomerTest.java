package customer.jpa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.*;

import javax.persistence.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CustomerTest {
    private static EntityManagerFactory entityManagerFactory;
    private static final String CUSTOMER_NAME = "Joe Smith";

    private EntityManager entityManager;

    @BeforeClass
    public static void beforeClass() {
        entityManagerFactory = Persistence.createEntityManagerFactory("CustomerPU");
        	String connectionUrl = "jdbc:hsqldb:mem:.";
        	String userName = "";
        	String password = "";
        	
            Connection con;
    		try {
    			con = DriverManager.getConnection(connectionUrl, userName, password);
    	        // Step 3:  Create a statement where the SQL statement will be provided
    	        Statement stmt = con.createStatement();
    	        stmt.executeUpdate("create table CUSTOMER (ID integer GENERATED BY DEFAULT AS IDENTITY(START WITH 100) PRIMARY KEY, NAME VARCHAR(50))");
    	        stmt.executeUpdate("create table ORDER_TABLE (ORDER_ID integer GENERATED BY DEFAULT AS IDENTITY(START WITH 100) PRIMARY KEY, SHIPPING_ADDRESS VARCHAR(50), CUSTOMER_ID INTEGER, FOREIGN KEY (CUSTOMER_ID) REFERENCES CUSTOMER (ID))");
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    }

    @Before
    public void beforeEachTest() {
        entityManager = entityManagerFactory.createEntityManager();
    }

    @After
    public void afterEachTest() {
        entityManager.close();
    }

    @Test
    public void testInsert() {
        entityManager.getTransaction().begin();
        Customer customer0 = new Customer();
        customer0.setId(1);
        customer0.setName(CUSTOMER_NAME);

        // Persist the customer
        entityManager.persist(customer0);

        // Create 2 orders
        Order order1 = new Order();
        order1.setId(100);
        order1.setAddress("123 Main St. Anytown, USA");

        Order order2 = new Order();
        order2.setId(200);
        order2.setAddress("567 1st St. Random City, USA");

        // Associate orders with the customer. The association 
        // must be set on both sides of the relationship: on the 
        // customer side for the orders to be persisted when 
        // transaction commits, and on the order side because it 
        // is the owning side.
        customer0.getOrders().add(order1);
        order1.setCustomer(customer0);

        customer0.getOrders().add(order2);
        order2.setCustomer(customer0);

        entityManager.getTransaction().commit();

        Customer verification = findCustomer();
        assertEquals("Customer name", CUSTOMER_NAME, verification.getName());
        assertEquals("Order size", 2, verification.getOrders().size());
    }

    @Test
    public void testRemove() {
        entityManager.getTransaction().begin();
        Customer joeSmithCustomer = findCustomer();
        assertTrue(joeSmithCustomer != null);
        entityManager.remove(joeSmithCustomer);
        entityManager.getTransaction().commit();

        try {
            findCustomer();
            Assert.fail("Shouldn't have found " + CUSTOMER_NAME);
        } catch (NoResultException nre) {

        }
    }


    private Customer findCustomer() {
        Query q = entityManager.createQuery("select c from Customer c where c.name = :name");
        q.setParameter("name", CustomerTest.CUSTOMER_NAME);
        return (Customer) q.getSingleResult();
    }
}
