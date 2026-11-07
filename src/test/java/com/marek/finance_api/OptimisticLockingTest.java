package com.marek.finance_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.marek.finance_api.model.Account;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class OptimisticLockingTest {

  @Autowired
  private EntityManagerFactory entityManagerFactory;

  @Test
  void shouldPreventUpdatingAccountWithStaleVersion() {

    EntityManager setupEm = entityManagerFactory.createEntityManager();
    EntityTransaction setupTx = setupEm.getTransaction();

    setupTx.begin();

    Account account = new Account();
    account.setName("Optimistic Lock Test");
    account.setBalance(new BigDecimal("100.00"));
    account.setCurrency("EUR");

    setupEm.persist(account);
    setupTx.commit();

    Long accountId = account.getId();
    setupEm.close();

    // User A loads the account
    EntityManager userAEm = entityManagerFactory.createEntityManager();
    EntityTransaction userATx = userAEm.getTransaction();

    userATx.begin();
    Account userAAccount = userAEm.find(Account.class, accountId);

    // User B loads the same account
    EntityManager userBEm = entityManagerFactory.createEntityManager();
    EntityTransaction userBTx = userBEm.getTransaction();

    userBTx.begin();
    Account userBAccount = userBEm.find(Account.class, accountId);

    assertEquals(new BigDecimal("100.00"), userAAccount.getBalance());
    assertEquals(new BigDecimal("100.00"), userBAccount.getBalance());

    assertEquals(0L, userAAccount.getVersion());
    assertEquals(0L, userBAccount.getVersion());

    // User A updates the balance
    userAAccount.setBalance(new BigDecimal("150.00"));
    userATx.commit();
    userAEm.close();

    // User B tries to update the stale account
    userBAccount.setBalance(new BigDecimal("200.00"));

    RollbackException exception = assertThrows(
        RollbackException.class,
        () -> userBTx.commit()
    );

    assertTrue(exception.getCause() instanceof OptimisticLockException);

    userBEm.close();

    // Verify the final database state
    EntityManager verifyEm = entityManagerFactory.createEntityManager();

    Account finalAccount = verifyEm.find(Account.class, accountId);

    assertEquals(new BigDecimal("150.00"), finalAccount.getBalance());
    assertEquals(1L, finalAccount.getVersion());

    verifyEm.close();

  }


}