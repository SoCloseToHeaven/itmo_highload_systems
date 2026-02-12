package ru.ifmo.highload.product.controller.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import ru.ifmo.highload.product.dto.category.CategoryCreateRequest;
import ru.ifmo.highload.product.dto.category.CategoryResponse;
import ru.ifmo.highload.product.dto.category.CategoryUpdateRequest;

/**
 * Category management API.
 */
@Tag(name = "Category Management", description = "API for managing product categories")
public interface CategoryApi {

    /** Returns all categories with pagination. */
    @Operation(summary = "Get all categories", description = "Get paginated list of all categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories successfully retrieved")
    })
    @GetMapping
    ResponseEntity<Page<CategoryResponse>> getCategories(
            @Parameter(description = "Pagination parameters", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""")             Pageable pageable);

    /** Creates a new category. */
    @Operation(summary = "Create category", description = "Create a new product category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden – authenticated but insufficient role (requires LOGISTICIAN or SUPERVISOR)"),
            @ApiResponse(responseCode = "409", description = "Category with this name already exists")
    })
    @PostMapping
    ResponseEntity<CategoryResponse> createCategory(
            @Parameter(description = "Category creation data")             @Valid @RequestBody CategoryCreateRequest request);

    /** Updates an existing category. */
    @Operation(summary = "Update category", description = "Update existing category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden – authenticated but insufficient role (requires LOGISTICIAN or SUPERVISOR)"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID to update") @PathVariable Long id,
            @Parameter(description = "Updated category data")             @Valid @RequestBody CategoryUpdateRequest request);

    /** Deletes category by ID. */
    @Operation(summary = "Delete category", description = "Delete category by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden – authenticated but insufficient role (requires LOGISTICIAN or SUPERVISOR)"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete category with child categories")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID to delete") @PathVariable Long id);
}
