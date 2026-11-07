
package com.marek.finance_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Account
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  private Long version;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal balance;

  @Column(nullable = false, length = 3)
  private String currency;

  public Long getId()
  {
    return id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public BigDecimal getBalance()
  {
    return balance;
  }

  public void setBalance(BigDecimal balance)
  {
    this.balance = balance;
  }

  public String getCurrency()
  {
    return currency;
  }

  public void setCurrency(String currency)
  {
    this.currency = currency;
  }

  public Long getVersion()
  {
    return version;
  }
}
