package com.marek.finance_api.service;

import com.marek.finance_api.dto.AccountDTO;
import com.marek.finance_api.dto.CreateAccountDTO;
import com.marek.finance_api.dto.UpdateAccountDTO;
import com.marek.finance_api.exception.ResourceNotFoundException;
import com.marek.finance_api.mapper.AccountMapper;
import com.marek.finance_api.model.Account;
import com.marek.finance_api.repository.AccountRepository;
import com.marek.finance_api.exception.ResourceConflictException;
import com.marek.finance_api.repository.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AccountService
{
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  public AccountService(
      AccountRepository accountRepository,
      TransactionRepository transactionRepository)
  {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }

  public AccountDTO createAccount(CreateAccountDTO dto)
  {
    Account account = AccountMapper.toEntity(dto);

    Account savedAccount = accountRepository.save(account);

    return AccountMapper.toDTO(savedAccount);
  }

  @Transactional
  public void deleteAccount(Long id)
  {
    Account account = accountRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Account with id " + id + " not found."
        ));

    if (transactionRepository.existsByAccountId(id))
    {
      throw new ResourceConflictException(
          "Account cannot be deleted because it contains transactions."
      );
    }

    accountRepository.delete(account);
  }

  @Transactional
  public AccountDTO updateAccount(Long id, UpdateAccountDTO dto)
  {
    Account account = accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
            "Account with id " + id + " not found."));

    account.setName(dto.name());

    Account savedAccount = accountRepository.save(account);

    return AccountMapper.toDTO(savedAccount);
  }

  public List<AccountDTO> getAllAccounts()
  {
    return accountRepository.findAll()
        .stream()
        .map(AccountMapper::toDTO)
        .toList();
  }

  public AccountDTO getAccountById(Long id)
  {
    Account account = accountRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Account with id " + id + " not found."
        ));

    return AccountMapper.toDTO(account);
  }
}