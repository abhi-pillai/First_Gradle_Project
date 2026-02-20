package com.myexpense.expensetracker.service;

import com.myexpense.expensetracker.model.Category;
import com.myexpense.expensetracker.repository.CategoryRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CategoryService {

    private final CategoryRepository repository;

    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "Food", "Rent", "Transport", "Entertainment",
            "Healthcare", "Shopping", "Utilities", "Education", "Other"
    );

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    // Seed predefined categories for a newly registered user
    public void seedDefaultCategories(String userId) throws IOException {
        for (String name : DEFAULT_CATEGORIES) {
            repository.save(new Category(userId, name, 0.0));
        }
    }

    // ===============================
    // CRUD (all scoped to userId)
    // ===============================
    public void addCategory(String userId, Category category) throws IOException {
        repository.save(category);
    }

    public List<Category> getAllCategories(String userId) {
        return repository.loadByUser(userId);
    }

    public void deleteCategory(String userId, String id) throws IOException {
        List<Category> updated = repository.loadByUser(userId).stream()
                .filter(c -> !c.getId().equals(id))
                .collect(Collectors.toList());
        repository.overwriteForUser(userId, updated);
    }

    public void updateCategory(String userId, Category updatedCategory) throws IOException {
        List<Category> updated = repository.loadByUser(userId).stream()
                .map(c -> c.getId().equals(updatedCategory.getId()) ? updatedCategory : c)
                .collect(Collectors.toList());
        repository.overwriteForUser(userId, updated);
    }

    public Optional<Category> findByName(String userId, String name) {
        return repository.loadByUser(userId).stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<String> getCategoryNames(String userId) {
        return repository.loadByUser(userId).stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }
}