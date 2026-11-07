
package com.marek.finance_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Transaction
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionType type;

  @Column(nullable = false, length = 255)
  private String description;

  @Column(nullable = false)
  private LocalDateTime date;

  @ManyToOne(optional = false)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  public Long getId()
  {
    return id;
  }

  public BigDecimal getAmount()
  {
    return amount;
  }

  public TransactionType getType()
  {
    return type;
  }

  public String getDescription()
  {
    return description;
  }

  public LocalDateTime getDate()
  {
    return date;
  }

  public Account getAccount()
  {
    return account;
  }

  public Category getCategory()
  {
    return category;
  }

  public void setAmount(BigDecimal amount)
  {
    this.amount = amount;
  }

  public void setType(TransactionType type)
  {
    this.type = type;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public void setDate(LocalDateTime date)
  {
    this.date = date;
  }

  public void setAccount(Account account)
  {
    this.account = account;
  }

  public void setCategory(Category category)
  {
    this.category = category;
  }
}
