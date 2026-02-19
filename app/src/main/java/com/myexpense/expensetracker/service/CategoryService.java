package com.myexpense.expensetracker.service;

import com.myexpense.expensetracker.model.Category;
import com.myexpense.expensetracker.repository.CategoryRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    // ===============================
    // CRUD
    // ===============================
    public void addCategory(Category category) throws IOException {
        repository.save(category);
    }

    public List<Category> getAllCategories() {
        return repository.loadAll();
    }

    public void deleteCategory(String id) throws IOException {
        List<Category> updated = repository.loadAll()
                .stream()
                .filter(c -> !c.getId().equals(id))
                .collect(Collectors.toList());
        repository.overwriteAll(updated);
    }

    public void updateCategory(Category updatedCategory) throws IOException {
        List<Category> updated = repository.loadAll()
                .stream()
                .map(c -> c.getId().equals(updatedCategory.getId()) ? updatedCategory : c)
                .collect(Collectors.toList());
        repository.overwriteAll(updated);
    }

    // ===============================
    // FILTER / SEARCH
    // ===============================
    public Optional<Category> findByName(String name) {
        return repository.loadAll()
                .stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<Category> searchByKeyword(String keyword) {
        return repository.loadAll()
                .stream()
                .filter(c -> c.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
}
