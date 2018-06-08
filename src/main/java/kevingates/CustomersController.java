package kevingates;

import io.ebean.Ebean;
import io.ebean.EbeanServer;

//@Inject
public class CustomersController {
	
	
	//final EbeanServer ebeanServer;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
				
		Customer customer = new Customer();
		customer.name = "billgates";
		//customer.save();
		Ebean.save(customer);
		echo("billgates");
	}

	public static void echo(String any) {
		System.out.println(any);
	}  
}
