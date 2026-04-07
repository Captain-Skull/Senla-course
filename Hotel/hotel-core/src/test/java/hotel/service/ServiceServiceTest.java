package hotel.service;

import enums.ServiceSort;
import enums.SortDirection;
import exceptions.DaoException;
import hotel.Service;
import hotel.dao.ServiceDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServiceServiceTest {

    @Mock
    private ServiceDao serviceDao;

    @InjectMocks
    private ServiceService serviceService;

    private Service service1;
    private Service service2;
    private Service service3;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service1 = new Service("S1", "Spa", 500, "Доступ в спа-зону");
        service2 = new Service("S2", "Dinner", 300, "Ужин в ресторане отеля");
        service3 = new Service("S3", "Laundry", 100, "Услуги прачечной");
    }

    @Test
    @DisplayName("Получение всех услуг")
    public void getAllServices_Positive() {
        when(serviceDao.findAll()).thenReturn(List.of(service1, service2, service3));
        assertEquals(3, serviceService.getAllServices().size());
    }

    @Test
    @DisplayName("Получение всех услуг - пусто")
    public void getAllServices_Negative() {
        when(serviceDao.findAll()).thenReturn(List.of());
        assertTrue(serviceService.getAllServices().isEmpty());
    }

    @Test
    @DisplayName("Получение услуги по ID")
    public void getServiceById_Positive() {
        when(serviceDao.findById("S1")).thenReturn(Optional.of(service1));
        Service service = serviceService.getServiceById("S1");
        assertEquals("S1", service.getId());
    }

    @Test
    @DisplayName("Получение услуги по ID - не найдена")
    public void getServiceById_Negative() {
        when(serviceDao.findById("S999")).thenReturn(Optional.empty());
        assertThrows(DaoException.class, () -> serviceService.getServiceById("S999"));
    }

    @Test
    @DisplayName("Сортировка услуг по ID по возрастанию")
    public void getSortedServicesById_Positive() {
        when(serviceDao.findAll()).thenReturn(List.of(service3, service1, service2));
        List<Service> sorted = serviceService.getSortedServices(ServiceSort.ID, SortDirection.ASC);
        assertEquals("S1", sorted.get(0).getId());
        assertEquals("S2", sorted.get(1).getId());
        assertEquals("S3", sorted.get(2).getId());
    }

    @Test
    @DisplayName("Сортировка услуг по цене по убыванию")
    public void getSortedServicesByPrice_Positive() {
        when(serviceDao.findAll()).thenReturn(List.of(service1, service2, service3));
        List<Service> sorted = serviceService.getSortedServices(ServiceSort.PRICE, SortDirection.DESC);
        assertEquals(500, sorted.get(0).getPrice());
        assertEquals(300, sorted.get(1).getPrice());
        assertEquals(100, sorted.get(2).getPrice());
    }

    @Test
    @DisplayName("Сортировка пустого списка")
    public void getSortedServices_Negative() {
        when(serviceDao.findAll()).thenReturn(List.of());
        assertTrue(serviceService.getSortedServices(ServiceSort.PRICE, SortDirection.ASC).isEmpty());
    }

    @Test
    @DisplayName("Обновление цены услуги")
    public void updateServicePrice_Positive() {
        when(serviceDao.findById("S1")).thenReturn(Optional.of(service1));
        when(serviceDao.update(any(Service.class))).thenReturn(service1);
        assertDoesNotThrow(() -> serviceService.updateServicePrice("S1", 750));
        assertEquals(750, service1.getPrice());
    }

    @Test
    @DisplayName("Обновление цены - не найдена")
    public void updateServicePrice_NotFound() {
        when(serviceDao.findById("S999")).thenReturn(Optional.empty());
        assertThrows(DaoException.class, () -> serviceService.updateServicePrice("S999", 750));
    }

    @Test
    @DisplayName("Обновление цены - ошибка DAO")
    public void updateServicePrice_DaoException() {
        when(serviceDao.findById("S1")).thenReturn(Optional.of(service1));
        when(serviceDao.update(any(Service.class))).thenThrow(new RuntimeException("DB error"));
        assertThrows(DaoException.class, () -> serviceService.updateServicePrice("S1", 750));
    }

    @Test
    @DisplayName("Обновление услуги")
    public void updateService_Positive() {
        when(serviceDao.update(any(Service.class))).thenReturn(service1);
        assertDoesNotThrow(() -> serviceService.updateService(service1));
    }

    @Test
    @DisplayName("Обновление услуги - ошибка DAO")
    public void updateService_DaoException() {
        when(serviceDao.update(any(Service.class))).thenThrow(new RuntimeException("DB error"));
        assertThrows(DaoException.class, () -> serviceService.updateService(service1));
    }

    @Test
    @DisplayName("Сохранение услуги")
    public void saveService_Positive() {
        when(serviceDao.save(service1)).thenReturn(service1);
        assertEquals("S1", serviceService.saveService(service1).getId());
    }

    @Test
    @DisplayName("Сохранение услуги - ошибка DAO")
    public void saveService_DaoException() {
        when(serviceDao.save(service1)).thenThrow(new RuntimeException("DB error"));
        assertThrows(DaoException.class, () -> serviceService.saveService(service1));
    }
}