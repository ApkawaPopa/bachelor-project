package minchakov.arkadii.amina.controller;

import jakarta.transaction.Transactional;
import minchakov.arkadii.amina.dto.ApiResponse;
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
    public ApiResponse<List<Example>> list() {
        return new ApiResponse<>(200, "Success", repository.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Example> read(@PathVariable Integer id) {
        return new ApiResponse<>(200, "Success", repository.findById(id).orElse(null));
    }

    @PostMapping
    public ApiResponse<Integer> create(@RequestBody Example example) {
        return new ApiResponse<>(201, "Success", repository.save(example).getId());
    }
}
