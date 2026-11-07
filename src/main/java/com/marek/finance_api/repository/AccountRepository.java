package com.marek.finance_api.repository;

import com.marek.finance_api.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>
{
}