package hotel.service;

import enums.ServiceSort;
import enums.SortDirection;
import exceptions.DaoException;
import hotel.Service;
import hotel.dao.ServiceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class ServiceService {

    private ServiceDao serviceDao;

    @Autowired
    ServiceService(ServiceDao serviceDao) {
        this.serviceDao = serviceDao;
    }

    public List<Service> getAllServices() {
        return serviceDao.findAll();
    }

    public Service getServiceById(String id) {
        return serviceDao.findById(id).orElseThrow(() -> new DaoException("Услуга не найдена: " + id));
    }

    public List<Service> getSortedServices(ServiceSort sortBy, SortDirection direction) {
        Comparator<Service> comparator = switch (sortBy) {
            case ServiceSort.ID -> Comparator.comparing(Service::getId);
            case ServiceSort.PRICE -> Comparator.comparing(Service::getPrice);
        };

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        List<Service> services = getAllServices().stream().sorted(comparator).toList();
        return services;
    }

    @Transactional
    public void updateServicePrice(String serviceId, int price) {
        try {
            Service service = getServiceById(serviceId);

            service.setPrice(price);
            serviceDao.update(service);
        } catch (Exception e) {
            throw new DaoException("Ошибка обновления цены услуги", e);
        }
    }

    @Transactional
    public void updateService(Service service) {
        try {
            serviceDao.update(service);
        } catch (Exception e) {
            throw new DaoException("Ошибка обновления услуги", e);
        }
    }

    @Transactional
    public Service saveService(Service service) {
        try {
            Service savedService = serviceDao.save(service);
            return savedService;
        } catch (Exception e) {
            throw new DaoException("Ошибка при сохранении услуги", e);
        }
    }
}
