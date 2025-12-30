package minchakov.arkadii.amina.controller;

import minchakov.arkadii.amina.dto.RestResponse;
import minchakov.arkadii.amina.model.Example;
import minchakov.arkadii.amina.repository.ExampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
    public RestResponse<List<Example>> list() {
        return new RestResponse<>(200, "Success", repository.findAll());
    }

    @GetMapping("/{id}")
    public RestResponse<Example> read(@PathVariable Integer id) {
        return RestResponse.success(repository.findById(id).orElse(null));
    }

    @PostMapping
    public RestResponse<Integer> create(@RequestBody Example example) {
        return RestResponse.created(repository.save(example).getId());
    }
}
