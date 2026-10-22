package com.example.demo.service;


import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repo;

    public CategoryService(CategoryRepository repo) {
        this.repo = repo;
    }

    public List<Category> findAll() {
        return repo.findAll();
    }

    public Category findById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "CategoryID phải lớn hơn 0"
            );
        }

        return repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Không tìm thấy danh mục có ID = " + id
            ));
    }

    public Category create(
        String name,
        String description,
        String status
    ) {
        validate(name, status);

        String normalizedName = name.trim();

        if (repo.existsByName(normalizedName, null)) {
            throw new IllegalArgumentException(
                "Tên danh mục đã tồn tại"
            );
        }

        int id = repo.insertAndGetId(
            normalizedName,
            description,
            normalizeStatus(status)
        );

        return findById(id);
    }

    public Category update(
        int id,
        String name,
        String description,
        String status
    ) {
        findById(id);
        validate(name, status);

        String normalizedName = name.trim();

        if (repo.existsByName(normalizedName, id)) {
            throw new IllegalArgumentException(
                "Tên danh mục đã được sử dụng"
            );
        }

        int affected = repo.update(
            id,
            normalizedName,
            description,
            normalizeStatus(status)
        );

        if (affected == 0) {
            throw new IllegalArgumentException(
                "Không cập nhật được danh mục"
            );
        }

        return findById(id);
    }

    public void delete(int id) {
        findById(id);

        if (repo.hasBooks(id)) {
            throw new IllegalArgumentException(
                "Không thể xóa danh mục đang có sách. " +
                "Hãy chuyển hoặc xóa các sách liên quan trước."
            );
        }

        repo.delete(id);
    }

    private void validate(String name, String status) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "CategoryName không được để trống"
            );
        }

        if (name.trim().length() > 255) {
            throw new IllegalArgumentException(
                "Tên danh mục không được vượt quá 255 ký tự"
            );
        }

        if (status != null && !status.isBlank()
            && !status.equals("ACTIVE")
            && !status.equals("INACTIVE")) {
            throw new IllegalArgumentException(
                "Status phải là ACTIVE hoặc INACTIVE"
            );
        }
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank()
            ? "ACTIVE"
            : status;
    }
}