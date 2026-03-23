package com.bank.consumer.dao;

import com.bank.consumer.model.Account;
import org.springframework.stereotype.Repository;

@Repository
public class AccountDao extends AbstractDao<Account, Long> {

    @Override
    protected Class<Account> getEntityClass() {
        return Account.class;
    }

}
