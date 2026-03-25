package com.bank.producer.dao;

import com.bank.producer.model.Account;
import org.springframework.stereotype.Repository;

@Repository
public class AccountDao extends AbstractDao<Account, Long> {

    @Override
    protected Class<Account> getEntityClass() {
        return Account.class;
    }
}
