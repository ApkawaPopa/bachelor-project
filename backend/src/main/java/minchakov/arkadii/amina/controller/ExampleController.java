package minchakov.arkadii.amina.controller;

import jakarta.transaction.Transactional;
import minchakov.arkadii.amina.dto.ApiResponseEntity;
import minchakov.arkadii.amina.model.Example;
import minchakov.arkadii.amina.repository.ExampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/v1/example")
public class ExampleController {

    private final ExampleRepository repository;

    @Autowired
    public ExampleController(ExampleRepository exampleRepository) {
        this.repository = exampleRepository;
    }

    @GetMapping
    public ApiResponseEntity<List<Example>> list() {
        return new ApiResponseEntity<>(200, "Success", repository.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponseEntity<Example> read(@PathVariable Integer id) {
        return new ApiResponseEntity<>(200, "Success", repository.findById(id).orElse(null));
    }

    @PostMapping
    public ApiResponseEntity<Integer> create(@RequestBody Example example) {
        return new ApiResponseEntity<>(201, "Success", repository.save(example).getId());
    }
}
