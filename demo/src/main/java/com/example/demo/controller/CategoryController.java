package com.example.demo.controller;



import com.example.demo.model.Category;
import com.example.demo.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    public record CategoryRequest(
        @NotBlank String categoryName,
        String description,
        @Pattern(regexp = "ACTIVE|INACTIVE")
        String status
    ) {}

    @GetMapping
    public List<Category> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Category findById(@PathVariable int id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(
        @Valid @RequestBody CategoryRequest request
    ) {
        return service.create(
            request.categoryName(),
            request.description(),
            request.status()
        );
    }

    @PutMapping("/{id}")
    public Category update(
        @PathVariable int id,
        @Valid @RequestBody CategoryRequest request
    ) {
        return service.update(
            id,
            request.categoryName(),
            request.description(),
            request.status()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        service.delete(id);
    }
}