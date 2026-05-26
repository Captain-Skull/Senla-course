package com.senla.pas.service;

import com.senla.pas.dao.AdDao;
import com.senla.pas.dao.PaymentDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.PaymentRequest;
import com.senla.pas.dto.response.PaymentResponse;
import com.senla.pas.entity.Ad;
import com.senla.pas.entity.Payment;
import com.senla.pas.entity.User;
import com.senla.pas.enums.PromotionPlan;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.PaymentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest extends AbstractServiceTest {

    @Mock
    private PaymentDao paymentDao;
    @Mock
    private AdDao adDao;
    @Mock
    private UserDao userDao;
    @Mock
    private PaymentMapper paymentMapper;
    @InjectMocks
    private PaymentService paymentService;


    @Test
    void getPaymentById_positive_owner() {
        authenticate(1L, "ROLE_USER");
        User owner = new User();
        owner.setId(1L);
        Payment payment = new Payment();
        payment.setUser(owner);
        PaymentResponse response = new PaymentResponse();
        when(paymentDao.findById(9L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(response);
        assertSame(response, paymentService.getPaymentById(9L));
    }

    @Test
    void getPaymentById_negative_forbidden() {
        authenticate(2L, "ROLE_USER");
        User owner = new User();
        owner.setId(1L);
        Payment payment = new Payment();
        payment.setUser(owner);
        when(paymentDao.findById(9L)).thenReturn(Optional.of(payment));
        assertThrows(ForbiddenException.class, () -> paymentService.getPaymentById(9L));
    }

    @Test
    void getPaymentById_npeSafety_nullId() {
        authenticate(1L, "ROLE_USER");
        when(paymentDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentById(null));
    }

    @Test
    void getPaymentsByAd_positive_owner() {
        authenticate(1L, "ROLE_USER");
        User owner = new User();
        owner.setId(1L);
        Ad ad = new Ad();
        ad.setUser(owner);
        when(adDao.findById(3L)).thenReturn(Optional.of(ad));
        when(paymentDao.findByAdId(3L)).thenReturn(List.of(new Payment()));
        when(paymentMapper.toResponseList(anyList())).thenReturn(List.of(new PaymentResponse()));
        assertEquals(1, paymentService.getPaymentsByAd(3L).size());
    }

    @Test
    void getPaymentsByAd_negative_forbidden() {
        authenticate(2L, "ROLE_USER");
        User owner = new User();
        owner.setId(1L);
        Ad ad = new Ad();
        ad.setUser(owner);
        when(adDao.findById(3L)).thenReturn(Optional.of(ad));
        assertThrows(ForbiddenException.class, () -> paymentService.getPaymentsByAd(3L));
    }

    @Test
    void getPaymentsByAd_npeSafety_nullId() {
        authenticate(1L, "ROLE_USER");
        when(adDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentsByAd(null));
    }

    @Test
    void getActivePaymentByAd_positive() {
        authenticate(1L, "ROLE_USER");
        User owner = new User();
        owner.setId(1L);
        Ad ad = new Ad();
        ad.setUser(owner);
        Payment payment = new Payment();
        when(adDao.findById(3L)).thenReturn(Optional.of(ad));
        when(paymentDao.findActiveByAdId(3L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(new PaymentResponse());
        assertDoesNotThrow(() -> paymentService.getActivePaymentByAd(3L));
    }

    @Test
    void getActivePaymentByAd_negative_notFound() {
        authenticate(1L, "ROLE_USER");
        User owner = new User();
        owner.setId(1L);
        Ad ad = new Ad();
        ad.setUser(owner);
        when(adDao.findById(3L)).thenReturn(Optional.of(ad));
        when(paymentDao.findActiveByAdId(3L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getActivePaymentByAd(3L));
    }

    @Test
    void getActivePaymentByAd_npeSafety_nullId() {
        authenticate(1L, "ROLE_USER");
        when(adDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getActivePaymentByAd(null));
    }

    @Test
    void getMyPayments_positive() {
        authenticate(4L, "ROLE_USER");
        when(paymentDao.findByUserId(4L)).thenReturn(Collections.emptyList());
        when(paymentMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());
        assertEquals(0, paymentService.getMyPayments().size());
    }

    @Test
    void getMyPayments_negative_daoFailure() {
        authenticate(4L, "ROLE_USER");
        when(paymentDao.findByUserId(4L)).thenThrow(new RuntimeException("db"));
        assertThrows(RuntimeException.class, () -> paymentService.getMyPayments());
    }

    @Test
    void getMyPayments_npeSafety_emptyList() {
        authenticate(4L, "ROLE_USER");
        when(paymentDao.findByUserId(4L)).thenReturn(Collections.emptyList());
        when(paymentMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> paymentService.getMyPayments());
    }

    @Test
    void createPayment_positive() {
        authenticate(1L, "ROLE_USER");
        User owner = new User();
        owner.setId(1L);
        Ad ad = new Ad();
        ad.setId(10L);
        ad.setUser(owner);
        ad.setIsActive(true);
        ad.setIsPremium(false);
        PaymentRequest request = new PaymentRequest(10L, PromotionPlan.DAY);
        Payment saved = new Payment();
        when(adDao.findByIdWithLock(10L)).thenReturn(Optional.of(ad));
        when(userDao.findById(1L)).thenReturn(Optional.of(owner));
        doAnswer(i -> {
            Payment p = i.getArgument(0);
            saved.setPlan(p.getPlan());
            return null;
        }).when(paymentDao).save(any(Payment.class));
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(new PaymentResponse());

        assertDoesNotThrow(() -> paymentService.createPayment(request));
        verify(adDao).updatePremiumStatus(10L, true);
    }

    @Test
    void createPayment_negative_forbiddenNotOwner() {
        authenticate(2L, "ROLE_USER");
        User owner = new User();
        owner.setId(1L);
        Ad ad = new Ad();
        ad.setUser(owner);
        ad.setIsActive(true);
        ad.setIsPremium(false);
        when(adDao.findByIdWithLock(10L)).thenReturn(Optional.of(ad));
        assertThrows(ForbiddenException.class, () -> paymentService.createPayment(new PaymentRequest(10L, PromotionPlan.DAY)));
    }

    @Test
    void createPayment_npeSafety_nullRequest() {
        authenticate(1L, "ROLE_USER");
        when(adDao.findByIdWithLock(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.createPayment(new PaymentRequest(null, PromotionPlan.DAY)));
    }

}
