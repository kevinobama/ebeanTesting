package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheType;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DefaultCacheHolderTest {

  private ThreadLocal<String> tenantId = new ThreadLocal<>();

  private final ServerCacheFactory cacheFactory = new DefaultServerCacheFactory();
  private final ServerCacheOptions defaultOptions = new ServerCacheOptions();

  @Test
  public void getCache_normal() throws Exception {

    DefaultCacheHolder holder = new DefaultCacheHolder(cacheFactory, defaultOptions, defaultOptions, null);

    DefaultServerCache cache = cache(holder, Customer.class, "customer");
    assertThat(cache.getName()).isEqualTo("customer_B");

    DefaultServerCache cache1 = cache(holder, Customer.class, "customer");
    assertThat(cache1).isSameAs(cache);

    DefaultServerCache cache2 = cache(holder, Contact.class, "contact");
    assertThat(cache1).isNotSameAs(cache2);
    assertThat(cache2.getName()).isEqualTo("contact_B");
  }

  private DefaultServerCache cache(DefaultCacheHolder holder, Class<?> type, String name) {
    return (DefaultServerCache) holder.getCache(type, name, ServerCacheType.BEAN);
  }

  @Test
  public void getCache_multiTenant() throws Exception {

    DefaultCacheHolder holder = new DefaultCacheHolder(cacheFactory, defaultOptions, defaultOptions, tenantId::get);

    tenantId.set("ten_1");
    DefaultServerCache cache = cache(holder, Customer.class, "customer");
    assertThat(cache.getName()).isEqualTo("customer_B");

    cache.put("1", "value-for-tenant1");
    cache.put("2", "an other value-for-tenant1");
    
    assertThat(cache.size()).isEqualTo(2);
    
    tenantId.set("ten_2");

    cache.put("1", "value-for-tenant2");
    cache.put("2", "an other value-for-tenant2");
    
    assertThat(cache.size()).isEqualTo(4);
        
    assertThat(cache.get("1")).isEqualTo("value-for-tenant2");
    assertThat(cache.get("2")).isEqualTo("an other value-for-tenant2");
    
    
    tenantId.set("ten_1");
    
    assertThat(cache.get("1")).isEqualTo("value-for-tenant1");
    assertThat(cache.get("2")).isEqualTo("an other value-for-tenant1");
    
    Exception exInThread[] = new Exception[1]; 
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          assertThat(cache.get("1")).isNull();
          tenantId.set("ten_2");

          cache.put("1", "value-for-tenant2");
          cache.put("2", "an other value-for-tenant2");
          
          tenantId.set(null);
          
          cache.clear();
        } catch (Exception e) {
          exInThread[0] = e;
        }
      };
    };
    
    // do some async work
    t.start();
    t.join();
    if (exInThread[0] != null) { 
      throw exInThread[0];
    }
    assertThat(cache.size()).isEqualTo(0);
  }


  @Test
  public void clearAll() throws Exception {
    DefaultCacheHolder holder = new DefaultCacheHolder(cacheFactory, defaultOptions, defaultOptions, null);
    DefaultServerCache cache = cache(holder, Customer.class, "customer");
    cache.put("foo", "foo");
    assertThat(cache.size()).isEqualTo(1);
    holder.clearAll();

    assertThat(cache.size()).isEqualTo(0);
  }

  @Test
  public void clearAll_multiTenant() throws Exception {
    DefaultCacheHolder holder = new DefaultCacheHolder(cacheFactory, defaultOptions, defaultOptions, tenantId::get);
    DefaultServerCache cache = cache(holder, Customer.class, "customer");
    cache.put("foo", "foo");
    assertThat(cache.size()).isEqualTo(1);

    holder.clearAll();
    assertThat(cache.size()).isEqualTo(0);
  }
 
}
