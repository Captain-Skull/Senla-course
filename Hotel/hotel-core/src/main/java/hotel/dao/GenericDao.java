package hotel.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDao<T, K> {

    T save(T entity);

    T update(T entity);

    boolean delete(K id);

    Optional<T> findById(K id);

    List<T> findAll();

    long count();
}