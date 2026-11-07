package com.marek.finance_api.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.marek.finance_api.model.Account;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@Testcontainers
class AccountRepositoryIntegrationTest
{
  @Container
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:18-alpine");

  @Autowired
  private AccountRepository accountRepository;

  @Test
  void shouldSaveAndFindAccount()
  {
    Account account = new Account();
    account.setName("Test Account");
    account.setBalance(new BigDecimal("1000.00"));
    account.setCurrency("EUR");

    Account saved = accountRepository.saveAndFlush(account);

    Account found = accountRepository.findById(saved.getId())
        .orElseThrow();

    assertNotNull(found.getId());
    assertEquals("Test Account", found.getName());
    assertEquals(new BigDecimal("1000.00"), found.getBalance());
    assertEquals("EUR", found.getCurrency());
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry)
  {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }
}