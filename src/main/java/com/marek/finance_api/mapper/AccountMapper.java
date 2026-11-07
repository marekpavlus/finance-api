package com.marek.finance_api.mapper;

import com.marek.finance_api.dto.AccountDTO;
import com.marek.finance_api.dto.CreateAccountDTO;
import com.marek.finance_api.model.Account;

import java.math.BigDecimal;

public class AccountMapper
{
  public static AccountDTO toDTO(Account account)
  {
    return new AccountDTO(
        account.getId(),
        account.getName(),
        account.getBalance(),
        account.getCurrency()
    );
  }

  public static Account toEntity(CreateAccountDTO dto)
  {
    Account account = new Account();

    account.setName(dto.name());
    account.setCurrency(dto.currency());
    account.setBalance(BigDecimal.ZERO);

    return account;
  }
}