package minchakov.arkadii.amina.service;

import java.util.Optional;

public interface CrudService<T, ID> {

    T create(T entity);

    Optional<T> read(ID id);

    T update(T entity);

    void delete(ID id);
}
