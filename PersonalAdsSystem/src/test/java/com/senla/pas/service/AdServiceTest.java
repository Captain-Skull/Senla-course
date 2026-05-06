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
import com.senla.pas.exception.AuthenticationException;
import com.senla.pas.exception.DaoException;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.AdMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdServiceTest extends AbstractServiceTest {

    @Mock
    private AdDao adDao;
    @Mock
    private UserDao userDao;
    @Mock
    private AdMapper adMapper;
    @InjectMocks
    private AdService adService;

    private List<Ad> sortedAds;
    private List<AdResponse> sortedResponses;

    @BeforeEach
    void setUp() {
        Ad adLowPrice = createAd(101L);
        Ad adMidPrice = createAd(102L);
        Ad adHighPrice = createAd(103L);

        AdResponse responseLowPrice = new AdResponse();
        AdResponse responseMidPrice = new AdResponse();
        AdResponse responseHighPrice = new AdResponse();

        sortedAds = List.of(adHighPrice, adMidPrice, adLowPrice);
        sortedResponses = List.of(responseHighPrice, responseMidPrice, responseLowPrice);
    }


    @Test
    void getAdsWithFilters_positive_adminSupportsAllSortModes() {
        authenticate(1L, "ROLE_ADMIN");
        for (AdSort sortBy : AdSort.values()) {
            for (SortDirection direction : SortDirection.values()) {
                reset(adDao, adMapper);
                when(adDao.findWithFilter(
                        AdCategory.ELECTRONICS,
                        "iphone",
                        10_000,
                        120_000,
                        false,
                        sortBy,
                        direction,
                        0,
                        10
                )).thenReturn(sortedAds);
                when(adMapper.toResponseList(sortedAds)).thenReturn(sortedResponses);

                List<AdResponse> result = adService.getAdsWithFilters(
                        AdCategory.ELECTRONICS,
                        "iphone",
                        10_000,
                        120_000,
                        false,
                        sortBy,
                        direction,
                        0,
                        10
                );

                assertSame(sortedResponses, result);
                verify(adDao).findWithFilter(
                        AdCategory.ELECTRONICS,
                        "iphone",
                        10_000,
                        120_000,
                        false,
                        sortBy,
                        direction,
                        0,
                        10
                );
            }
        }
    }

    @Test
    void getAdsWithFilters_positive_nonAdminForcesActiveEvenWithFiltersAndSorting() {
        authenticate(10L, "ROLE_USER");
        when(adDao.findWithFilter(
                AdCategory.TRANSPORT,
                "bmw",
                1_000,
                5_000_000,
                true,
                AdSort.PRICE,
                SortDirection.DESC,
                1,
                25
        )).thenReturn(sortedAds);
        when(adMapper.toResponseList(sortedAds)).thenReturn(sortedResponses);

        List<AdResponse> result = adService.getAdsWithFilters(
                AdCategory.TRANSPORT,
                "bmw",
                1_000,
                5_000_000,
                false,
                AdSort.PRICE,
                SortDirection.DESC,
                1,
                25
        );

        assertSame(sortedResponses, result);
        verify(adDao).findWithFilter(
                AdCategory.TRANSPORT,
                "bmw",
                1_000,
                5_000_000,
                true,
                AdSort.PRICE,
                SortDirection.DESC,
                1,
                25
        );
    }

    @Test
    void getAdsWithFilters_negative_daoFailure() {
        authenticate(1L, "ROLE_ADMIN");
        when(adDao.findWithFilter(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new DaoException("db"));
        assertThrows(DaoException.class, () -> adService.getAdsWithFilters(null, null, null, null, true, null, null, 0, 10));
    }

    @Test
    void getAdsWithFilters_npeSafety_nullFilters() {
        authenticate(1L, "ROLE_USER");
        when(adDao.findWithFilter(null, null, null, null, true, null, null, 0, 10)).thenReturn(Collections.emptyList());
        when(adMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> adService.getAdsWithFilters(null, null, null, null, null, null, null, 0, 10));
    }

    @Test
    void getAdsWithFilters_positive_withOnlySortingNoFilters() {
        authenticate(1L, "ROLE_ADMIN");
        when(adDao.findWithFilter(null, null, null, null, true, AdSort.TITLE, SortDirection.ASC, 0, 50)).thenReturn(sortedAds);
        when(adMapper.toResponseList(sortedAds)).thenReturn(sortedResponses);

        List<AdResponse> result = adService.getAdsWithFilters(
                null,
                null,
                null,
                null,
                true,
                AdSort.TITLE,
                SortDirection.ASC,
                0,
                50
        );

        assertSame(sortedResponses, result);
    }

    @Test
    void getAdById_positive() {
        Ad ad = new Ad();
        AdResponse response = new AdResponse();
        when(adDao.findById(1L)).thenReturn(Optional.of(ad));
        when(adMapper.toResponse(ad)).thenReturn(response);
        assertSame(response, adService.getAdById(1L));
    }

    @Test
    void getAdById_negative_notFound() {
        when(adDao.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adService.getAdById(1L));
    }

    @Test
    void getAdById_npeSafety_nullId() {
        when(adDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adService.getAdById(null));
    }

    @Test
    void getAdsByUser_positive() {
        Ad ad = new Ad();
        AdResponse response = new AdResponse();
        when(adDao.findByUserId(1L)).thenReturn(List.of(ad));
        when(adMapper.toResponseList(List.of(ad))).thenReturn(List.of(response));
        assertEquals(1, adService.getAdsByUser(1L).size());
    }

    @Test
    void getAdsByUser_negative_daoFailure() {
        when(adDao.findByUserId(1L)).thenThrow(new RuntimeException("db"));
        assertThrows(RuntimeException.class, () -> adService.getAdsByUser(1L));
    }

    @Test
    void getAdsByUser_npeSafety_nullUserId() {
        when(adDao.findByUserId(null)).thenReturn(Collections.emptyList());
        when(adMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> adService.getAdsByUser(null));
    }

    @Test
    void getMyAds_positive() {
        authenticate(5L, "ROLE_USER");
        when(adDao.findByUserId(5L)).thenReturn(Collections.emptyList());
        when(adMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());
        assertEquals(0, adService.getMyAds().size());
    }

    @Test
    void getMyAds_negative_unauthenticated() {
        assertThrows(AuthenticationException.class, () -> adService.getMyAds());
    }

    @Test
    void getMyAds_npeSafety_nonCustomPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "p"));
        assertThrows(AuthenticationException.class, () -> adService.getMyAds());
    }

    @Test
    void createAd_positive() {
        authenticate(1L, "ROLE_USER");
        User user = new User();
        Ad ad = new Ad();
        ad.setId(100L);
        CreateAdRequest request = new CreateAdRequest();
        AdResponse response = new AdResponse();
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(adMapper.toEntity(request)).thenReturn(ad);
        when(adMapper.toResponse(ad)).thenReturn(response);

        AdResponse result = adService.createAd(request);

        assertSame(response, result);
        verify(adDao).save(ad);
        assertSame(user, ad.getUser());
    }

    @Test
    void createAd_negative_userNotFound() {
        authenticate(1L, "ROLE_USER");
        when(userDao.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adService.createAd(new CreateAdRequest()));
    }

    @Test
    void createAd_npeSafety_nullRequest() {
        authenticate(1L, "ROLE_USER");
        User user = new User();
        Ad ad = new Ad();
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(adMapper.toEntity(null)).thenReturn(ad);
        when(adMapper.toResponse(ad)).thenReturn(new AdResponse());
        assertDoesNotThrow(() -> adService.createAd(null));
    }

    @Test
    void updateAd_positive() {
        authenticate(1L, "ROLE_USER");
        Ad ad = new Ad();
        UpdateAdRequest request = new UpdateAdRequest();
        AdResponse response = new AdResponse();
        when(adDao.findById(7L)).thenReturn(Optional.of(ad));
        when(adDao.isOwner(7L, 1L)).thenReturn(true);
        when(adMapper.toResponse(ad)).thenReturn(response);
        assertSame(response, adService.updateAd(7L, request));
        verify(adMapper).updateEntity(request, ad);
        verify(adDao).update(ad);
    }

    @Test
    void updateAd_negative_forbidden() {
        authenticate(1L, "ROLE_USER");
        when(adDao.findById(7L)).thenReturn(Optional.of(new Ad()));
        when(adDao.isOwner(7L, 1L)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> adService.updateAd(7L, new UpdateAdRequest()));
    }

    @Test
    void updateAd_npeSafety_nullRequestNotFoundFirst() {
        when(adDao.findById(7L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adService.updateAd(7L, null));
    }

    @Test
    void deleteAd_positive_owner() {
        authenticate(1L, "ROLE_USER");
        Ad ad = new Ad();
        AdResponse response = new AdResponse();
        when(adDao.findById(2L)).thenReturn(Optional.of(ad));
        when(adDao.isOwner(2L, 1L)).thenReturn(true);
        when(adMapper.toResponse(ad)).thenReturn(response);
        assertSame(response, adService.deleteAd(2L));
        verify(adDao).delete(2L);
    }

    @Test
    void deleteAd_negative_forbidden() {
        authenticate(3L, "ROLE_USER");
        when(adDao.findById(2L)).thenReturn(Optional.of(new Ad()));
        when(adDao.isOwner(2L, 3L)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> adService.deleteAd(2L));
    }

    @Test
    void deleteAd_npeSafety_nullId() {
        when(adDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adService.deleteAd(null));
    }


    private Ad createAd(Long id) {
        Ad ad = new Ad();
        ad.setId(id);
        return ad;
    }
}
