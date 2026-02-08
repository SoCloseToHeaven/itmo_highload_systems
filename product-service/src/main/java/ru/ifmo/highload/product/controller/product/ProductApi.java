package ru.ifmo.highload.product.controller.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.highload.product.dto.product.ProductResponse;
import ru.ifmo.highload.product.dto.product.ProductUpdateRequest;

/**
 * Product management API.
 */
@Tag(name = "Product Management", description = "API for managing products")
public interface ProductApi {

    /** Returns paginated products for a category. */
    @Operation(summary = "Get products by category", description = "Get paginated list of products for a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/category/{categoryId}")
    ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "Pagination parameters", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""")             Pageable pageable);

    /** Returns product by ID. */
    @Operation(summary = "Get product by ID", description = "Get detailed product information by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "Product ID")             @PathVariable Long id);

    /** Updates product (requires LOGISTICIAN or SUPERVISOR). */
    @Operation(summary = "Update product", description = "Update product information (for administrators)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product successfully updated"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PutMapping("/{id}")
    ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID to update") @PathVariable Long id,
            @Parameter(description = "Updated product data")             @RequestBody ProductUpdateRequest request);

    /** Searches products by name with pagination. */
    @Operation(summary = "Search products", description = "Search products by name with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products successfully retrieved")
    })
    @GetMapping("/search")
    ResponseEntity<Page<ProductResponse>> searchProducts(
            @Parameter(description = "Product name to search") @RequestParam String name,
            @Parameter(description = "Pagination parameters", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""")             Pageable pageable);

    /** Returns all products with pagination. */
    @Operation(summary = "Get all products", description = "Get paginated list of all products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products successfully retrieved")
    })
    @GetMapping
    ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Parameter(description = "Pagination parameters", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""") Pageable pageable);
}
