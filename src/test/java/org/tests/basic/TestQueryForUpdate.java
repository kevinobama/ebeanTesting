package org.tests.basic;

import io.ebean.AcquireLockException;
import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TestQueryForUpdate extends BaseTestCase {


  @Test
  @ForPlatform({
    Platform.H2, Platform.ORACLE, Platform.POSTGRES, Platform.SQLSERVER, Platform.MYSQL
  })
  public void testForUpdate() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .forUpdate()
      .order().desc("id");

    query.findList();
    if (isSqlServer()) {
      assertThat(sqlOf(query)).contains("with (updlock)");
    } else {
      assertThat(sqlOf(query)).contains("for update");
    }
  }

  @Test
  @ForPlatform({
    Platform.H2, Platform.POSTGRES, Platform.SQLSERVER, Platform.ORACLE
  })
  public void testForUpdate_noWait_noMaxRows() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .forUpdateNoWait()
      .order().desc("id");

    query.findList();
    if (isOracle()) {
      assertThat(sqlOf(query)).contains("for update nowait");
    } else if (isH2()) {
      assertThat(sqlOf(query)).contains("for update");
    } else if (isSqlServer()) {
      assertThat(sqlOf(query)).contains("with (updlock,nowait)");
    } else {
      assertThat(sqlOf(query)).contains("for update nowait");
    }
  }

  @Test
  @ForPlatform({
    Platform.H2, Platform.POSTGRES, Platform.SQLSERVER
  })
  public void testForUpdate_noWait() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getDefaultServer();

    try (Transaction txn = Ebean.beginTransaction()) {
      Query<Customer> query = Ebean.find(Customer.class)
        .forUpdateNoWait()
        .setMaxRows(1)
        .order().desc("id");

      List<Customer> list = query.findList();
      Customer first = list.get(0);
      if (isSqlServer()) {
        assertThat(sqlOf(query)).contains("with (updlock,nowait)");
      } else if (isH2()) {
        assertThat(sqlOf(query)).contains("for update");
      } else {
        assertThat(sqlOf(query)).contains("for update nowait");
      }
      // create a 2nd transaction to test that the
      // row is locked and we can't acquire it
      try (Transaction txn2 = server.createTransaction()) {
        logger.info("... attempt another acquire using 2nd transaction");
        Query<Customer> query2 =
          server.find(Customer.class)
            .where().idEq(first.getId())
            .forUpdateNoWait();

        server.findOne(query2, txn2);
        assertTrue(false); // never get here
      } catch (AcquireLockException e) {
        logger.info("... got AcquireLockException " + e);
      }
      txn.commit();
    }
  }
}
