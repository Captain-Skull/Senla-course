package com.bank.producer.dao;

import com.bank.producer.dao.AbstractDao;
import com.bank.producer.exceptions.DaoException;
import com.bank.producer.model.Transfer;
import org.springframework.stereotype.Repository;

@Repository
public class TransferDao extends AbstractDao<Transfer, Long> {

    @Override
    protected Class<Transfer> getEntityClass() {
        return Transfer.class;
    }

    public long findMaxTransferId() {
        try {
            Long maxId = entityManager.createQuery("SELECT MAX(t.id) FROM Transfer t", Long.class)
                    .getSingleResult();
            return maxId != null ? maxId : 0L;
        } catch (Exception e) {
            logger.error("Ошибка при поиске максимального ID перевода", e);
            throw new DaoException("Ошибка при поиске максимального ID перевода", e);
        }
    }
}
