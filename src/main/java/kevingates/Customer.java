package kevingates;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity @Table(name="customers")
public class Customer extends BaseModel {

  public String name;
}