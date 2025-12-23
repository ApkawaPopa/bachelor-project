package minchakov.arkadii.amina.repository;

import minchakov.arkadii.amina.model.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExampleRepository extends JpaRepository<Example, Integer> {
}
