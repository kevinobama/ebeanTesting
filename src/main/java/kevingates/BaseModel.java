package kevingates;

import io.ebean.Model;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseModel extends Model {

      @Id Long id;

//      @Version Long version;
//      @WhenCreated Timestamp whenCreated;
//      @WhenUpdated Timestamp whenUpdated;

 }