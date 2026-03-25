package com.bank.consumer.dao;

import com.bank.consumer.model.Transfer;
import com.bank.consumer.dao.AbstractDao;
import com.bank.consumer.exceptions.DaoException;
import org.springframework.stereotype.Repository;

@Repository
public class TransferDao extends AbstractDao<Transfer, Long> {
    @Override
    protected Class<Transfer> getEntityClass() {
        return Transfer.class;
    }
}
