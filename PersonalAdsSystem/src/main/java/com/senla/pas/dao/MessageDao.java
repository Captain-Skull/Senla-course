package com.senla.pas.dao;

import com.senla.pas.entity.Message;
import com.senla.pas.exception.DaoException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageDao extends AbstractJpaDao<Message, Long> {

    private static final String FIND_BY_CHAT_ID_JPQL = "SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timestamp ASC";

    @Override
    protected Class<Message> getEntityClass() {
        return Message.class;
    }

    public List<Message> findByChatId(Long chatId) {
        try {
            return entityManager.createQuery(FIND_BY_CHAT_ID_JPQL, Message.class)
                    .setParameter("chatId", chatId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Ошибка получения сообщений по chatId: {}", chatId, e);
            throw new DaoException("Ошибка получения сообщений по chatId: " + chatId, e);
        }
    }
}
