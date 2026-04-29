package com.senla.pas.service;

import com.senla.pas.dao.AdDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.CreateAdRequest;
import com.senla.pas.dto.request.UpdateAdRequest;
import com.senla.pas.dto.response.AdResponse;
import com.senla.pas.entity.Ad;
import com.senla.pas.entity.User;
import com.senla.pas.enums.AdCategory;
import com.senla.pas.enums.AdSort;
import com.senla.pas.enums.SortDirection;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.AdMapper;
import com.senla.pas.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdService {

    private final AdDao adDao;
    private final UserDao userDao;
    private final AdMapper adMapper;
    private static final Logger logger = LoggerFactory.getLogger(AdService.class);

    @Autowired
    public AdService(AdDao adDao, UserDao userDao, AdMapper adMapper) {
        this.adDao = adDao;
        this.userDao = userDao;
        this.adMapper = adMapper;
    }

    public List<AdResponse> getAdsWithFilters(
            AdCategory category,
            String searchText,
            Integer minPrice,
            Integer maxPrice,
            Boolean isActive,
            AdSort sortBy,
            SortDirection sortDirection,
            int page,
            int size
    ) {
        logger.info("Получение отфильтрованных объявлений");
        return adMapper.toResponseList(adDao.findWithFilter(category, searchText, minPrice, maxPrice, isActive, sortBy, sortDirection, page, size));
    }

    public AdResponse getAdById(Long adId) {
        logger.info("Получение объявления с ID: {}", adId);
        return adMapper.toResponse(adDao.findById(adId).orElseThrow(() -> new ResourceNotFoundException("Объявление не найдено: " + adId)));
    }

    public List<AdResponse> getAdsByUserId(long userId) {
        logger.info("Получение объявлений пользователя с ID: {} ", userId);
        return adMapper.toResponseList(adDao.findByUserId(userId));
    }

    public List<AdResponse> getMyAds() {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Получение своих объявленией пользователем с ID: {}", userId);
        return adMapper.toResponseList(adDao.findByUserId(userId));
    }

    @Transactional
    public AdResponse createAd(CreateAdRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userDao.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + userId));

        Ad ad = adMapper.toEntity(request);
        ad.setUser(user);

        adDao.save(ad);

        logger.info("Создано объявление {}. Пользователь {}", ad.getId(), userId);
        return adMapper.toResponse(ad);
    }

    @Transactional
    public AdResponse updateAd(Long adId, UpdateAdRequest request) {
        Ad ad = adDao.findById(adId).orElseThrow(() -> new ResourceNotFoundException("Объявление не найдено: " + adId));
        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (!adDao.isOwner(adId, currentUserId)) {
            throw new ForbiddenException("Редактировать объявление может только владелец");
        }
        adMapper.updateEntity(request, ad);

        adDao.update(ad);

        logger.info("Объявление {} обновлено", adId);
        return adMapper.toResponse(ad);
    }

    @Transactional
    public AdResponse deleteAd(Long adId) {
        Ad ad = adDao.findById(adId).orElseThrow(() -> new ResourceNotFoundException("Объявление не найдено: " + adId));
        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (!adDao.isOwner(adId, currentUserId) && !SecurityUtils.hasRole("ROLE_ADMIN")) {
            throw new ForbiddenException("Нет прав для удаления этого объявления");
        }

        adDao.delete(adId);
        logger.info("Объявление {} успешно удалено", adId);
        return adMapper.toResponse(ad);
    }
}
