package com.senla.pas.service;

import com.senla.pas.dao.AdDao;
import com.senla.pas.dao.ChatDao;
import com.senla.pas.dao.SaleHistoryDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.SaleHistoryRequest;
import com.senla.pas.dto.response.SaleHistoryResponse;
import com.senla.pas.entity.Ad;
import com.senla.pas.entity.Chat;
import com.senla.pas.entity.SaleHistory;
import com.senla.pas.entity.User;
import com.senla.pas.exception.BadRequestException;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.SaleHistoryMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SaleHistoryServiceTest extends AbstractServiceTest {

    @Mock
    private SaleHistoryDao saleHistoryDao;
    @Mock
    private AdDao adDao;
    @Mock
    private UserDao userDao;
    @Mock
    private ChatDao chatDao;
    @Mock
    private SaleHistoryMapper saleHistoryMapper;
    @InjectMocks
    private SaleHistoryService saleHistoryService;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMySales_positive() {
        authenticate(1L, "ROLE_USER");
        when(saleHistoryDao.findBySellerId(1L)).thenReturn(List.of(new SaleHistory()));
        when(saleHistoryMapper.toResponseList(anyList())).thenReturn(List.of(new SaleHistoryResponse()));
        assertEquals(1, saleHistoryService.getMySales().size());
    }

    @Test
    void getMySales_negative_daoFailure() {
        authenticate(1L, "ROLE_USER");
        when(saleHistoryDao.findBySellerId(1L)).thenThrow(new RuntimeException("db"));
        assertThrows(RuntimeException.class, () -> saleHistoryService.getMySales());
    }

    @Test
    void getMySales_npeSafety_empty() {
        authenticate(1L, "ROLE_USER");
        when(saleHistoryDao.findBySellerId(1L)).thenReturn(Collections.emptyList());
        when(saleHistoryMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> saleHistoryService.getMySales());
    }

    @Test
    void getMyPurchases_positive() {
        authenticate(1L, "ROLE_USER");
        when(saleHistoryDao.findByBuyerId(1L)).thenReturn(List.of(new SaleHistory()));
        when(saleHistoryMapper.toResponseList(anyList())).thenReturn(List.of(new SaleHistoryResponse()));
        assertEquals(1, saleHistoryService.getMyPurchases().size());
    }

    @Test
    void getMyPurchases_negative_daoFailure() {
        authenticate(1L, "ROLE_USER");
        when(saleHistoryDao.findByBuyerId(1L)).thenThrow(new RuntimeException("db"));
        assertThrows(RuntimeException.class, () -> saleHistoryService.getMyPurchases());
    }

    @Test
    void getMyPurchases_npeSafety_empty() {
        authenticate(1L, "ROLE_USER");
        when(saleHistoryDao.findByBuyerId(1L)).thenReturn(Collections.emptyList());
        when(saleHistoryMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> saleHistoryService.getMyPurchases());
    }

    @Test
    void getSaleById_positive_buyer() {
        authenticate(2L, "ROLE_USER");
        User buyer = new User();
        buyer.setId(2L);
        User seller = new User();
        seller.setId(3L);
        SaleHistory sale = new SaleHistory();
        sale.setBuyer(buyer);
        sale.setSeller(seller);
        when(saleHistoryDao.findById(10L)).thenReturn(Optional.of(sale));
        when(saleHistoryMapper.toResponse(sale)).thenReturn(new SaleHistoryResponse());
        assertDoesNotThrow(() -> saleHistoryService.getSaleById(10L));
    }

    @Test
    void getSaleById_negative_forbidden() {
        authenticate(4L, "ROLE_USER");
        User buyer = new User();
        buyer.setId(2L);
        User seller = new User();
        seller.setId(3L);
        SaleHistory sale = new SaleHistory();
        sale.setBuyer(buyer);
        sale.setSeller(seller);
        when(saleHistoryDao.findById(10L)).thenReturn(Optional.of(sale));
        assertThrows(ForbiddenException.class, () -> saleHistoryService.getSaleById(10L));
    }

    @Test
    void getSaleById_npeSafety_nullId() {
        authenticate(2L, "ROLE_USER");
        when(saleHistoryDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> saleHistoryService.getSaleById(null));
    }

    @Test
    void buyViaChat_positive() {
        authenticate(2L, "ROLE_USER");
        User buyer = new User();
        buyer.setId(2L);
        User seller = new User();
        seller.setId(3L);
        Ad ad = new Ad();
        ad.setId(8L);
        ad.setTitle("title");
        ad.setPrice(100);
        ad.setUser(seller);
        ad.setIsActive(true);
        Chat chat = new Chat();
        chat.setBuyer(buyer);
        chat.setSeller(seller);
        chat.setAd(ad);
        when(chatDao.findById(5L)).thenReturn(Optional.of(chat));
        when(adDao.findByIdWithLock(8L)).thenReturn(Optional.of(ad));
        when(saleHistoryDao.existsByAdId(8L)).thenReturn(false);
        when(saleHistoryMapper.toResponse(any())).thenReturn(new SaleHistoryResponse());
        assertDoesNotThrow(() -> saleHistoryService.buyViaChat(5L, new SaleHistoryRequest(90)));
        verify(adDao).update(ad);
    }

    @Test
    void buyViaChat_negative_forbidden_nonBuyer() {
        authenticate(9L, "ROLE_USER");
        User buyer = new User();
        buyer.setId(2L);
        Chat chat = new Chat();
        chat.setBuyer(buyer);
        when(chatDao.findById(5L)).thenReturn(Optional.of(chat));
        assertThrows(ForbiddenException.class, () -> saleHistoryService.buyViaChat(5L, new SaleHistoryRequest(90)));
    }

    @Test
    void buyViaChat_npeSafety_nullRequestNotUsedWhenChatMissing() {
        authenticate(2L, "ROLE_USER");
        when(chatDao.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> saleHistoryService.buyViaChat(5L, null));
    }

    @Test
    void buyDirectly_positive() {
        authenticate(2L, "ROLE_USER");
        User buyer = new User();
        buyer.setId(2L);
        User seller = new User();
        seller.setId(3L);
        Ad ad = new Ad();
        ad.setId(8L);
        ad.setTitle("title");
        ad.setPrice(100);
        ad.setUser(seller);
        ad.setIsActive(true);
        when(adDao.findByIdWithLock(8L)).thenReturn(Optional.of(ad));
        when(saleHistoryDao.existsByAdId(8L)).thenReturn(false);
        when(userDao.findById(2L)).thenReturn(Optional.of(buyer));
        when(saleHistoryMapper.toResponse(any())).thenReturn(new SaleHistoryResponse());
        assertDoesNotThrow(() -> saleHistoryService.buyDirectly(8L));
        verify(adDao).update(ad);
    }

    @Test
    void buyDirectly_negative_buyOwnAd() {
        authenticate(2L, "ROLE_USER");
        User seller = new User();
        seller.setId(2L);
        Ad ad = new Ad();
        ad.setId(8L);
        ad.setUser(seller);
        ad.setIsActive(true);
        when(adDao.findByIdWithLock(8L)).thenReturn(Optional.of(ad));
        assertThrows(BadRequestException.class, () -> saleHistoryService.buyDirectly(8L));
    }

    @Test
    void buyDirectly_npeSafety_nullAdId() {
        authenticate(2L, "ROLE_USER");
        when(adDao.findByIdWithLock(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> saleHistoryService.buyDirectly(null));
    }

}
