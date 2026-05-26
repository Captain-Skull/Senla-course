package com.senla.pas.scheduler;

import com.senla.pas.dao.AdDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PremiumExpirationScheduler {

    private final AdDao adDao;
    private static final Logger logger = LoggerFactory.getLogger(PremiumExpirationScheduler.class);

    @Autowired
    public PremiumExpirationScheduler(AdDao adDao) {
        this.adDao = adDao;
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Moscow")
    @Transactional
    public void deactivateExpiredPremiumAds() {
        logger.info("Деактивация просроченных премиум объявлений");
        try {
            int count = adDao.deactivateExpiredPremium();
            if (count > 0) {
            logger.info("Деактивировано {} просроченных премиум объявлений", count);
            } else {
                logger.debug("Просроченных премиум объявлений не найдено");
            }
        } catch (Exception e) {
            logger.error("Ошибка при деактивации просроченных премиум объявлений: {}", e.getMessage(), e);
        }
    }
}
