package com.bank.consumer.dao;

import com.bank.consumer.exceptions.DaoException;
import com.bank.consumer.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AccountDao extends AbstractDao<Account, Long> {

    @Override
    protected Class<Account> getEntityClass() {
        return Account.class;
    }

    public Optional<Account> findByIdForUpdate(long id) {
        try {
            Account account = entityManager.find(Account.class, id, LockModeType.PESSIMISTIC_WRITE);
            return Optional.ofNullable(account);
        } catch (Exception e) {
            logger.error("Error finding account by ID for update: " + id, e);
            throw new DaoException("Error finding account by ID for update: " + id, e);
        }
    }

}
