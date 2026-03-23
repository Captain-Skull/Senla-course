package com.bank.consumer.dao;

import com.bank.consumer.model.Transfer;
import com.bank.consumer.dao.AbstractDao;
import com.bank.consumer.exceptions.DaoException;
import org.springframework.stereotype.Repository;

@Repository
public class TransferDao extends AbstractDao<Transfer, Long> {

    private static final String FIND_BY_SENDER_ACCOUNT_ID_QUERY = "SELECT t FROM Transfer t WHERE t.senderAccountId = :senderAccountId";
    private static final String FIND_BY_RECIPIENT_ACCOUNT_ID_QUERY = "SELECT t FROM Transfer t WHERE t.recipientAccountId = :recipientAccountId";

    @Override
    protected Class<Transfer> getEntityClass() {
        return Transfer.class;
    }

    public Transfer findBySenderAccountId(long senderAccountId) {
        try {
            return entityManager.createQuery(FIND_BY_SENDER_ACCOUNT_ID_QUERY, Transfer.class)
                    .setParameter("senderAccountId", senderAccountId)
                    .getSingleResult();
        } catch (Exception e) {
            logger.error("Ошибка поиска по ID отправителя: " + senderAccountId, e);
            throw new DaoException("Ошибка поиска по ID отправителя: " + senderAccountId, e);
        }
    }

    public Transfer findByRecipientAccountId(long recipientAccountId) {
        try {
            return entityManager.createQuery(FIND_BY_RECIPIENT_ACCOUNT_ID_QUERY, Transfer.class)
                    .setParameter("recipientAccountId", recipientAccountId)
                    .getSingleResult();
        } catch (Exception e) {
            logger.error("Ошибка поиска по ID получателя: " + recipientAccountId, e);
            throw new DaoException("Ошибка поиска по ID получателя: " + recipientAccountId, e);
        }
    }

}
