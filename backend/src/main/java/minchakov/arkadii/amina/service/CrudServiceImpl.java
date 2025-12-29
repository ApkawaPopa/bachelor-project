package minchakov.arkadii.amina.service;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public class CrudServiceImpl<T, ID> implements CrudService<T, ID> {

    private final CrudRepository<T, ID> repository;

    public CrudServiceImpl(CrudRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public T create(T entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<T> read(ID id) {
        return repository.findById(id);
    }

    @Override
    public T update(T entity) {
        return repository.save(entity);
    }

    @Override
    public void delete(ID id) {
        repository.deleteById(id);
    }
}
