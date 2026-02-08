package ru.ifmo.highload.discount.controller;

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
import ru.ifmo.highload.discount.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.discount.dto.discount.DiscountResponse;
import ru.ifmo.highload.discount.dto.discount.DiscountUpdateRequest;

import java.util.List;

/**
 * Discount management API (administrators).
 */
@Tag(name = "Discount Management", description = "API for managing product discounts (for administrators)")
public interface DiscountApi {

    /** Creates a new discount. */
    @Operation(summary = "Create discount", description = "Create a new discount for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Discount successfully created"),
            @ApiResponse(responseCode = "404", description = "Product or price not found"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    @PostMapping
    ResponseEntity<DiscountResponse> createDiscount(
            @Parameter(description = "Discount creation data")             @Valid @RequestBody DiscountCreateRequest request);

    /** Updates an existing discount. */
    @Operation(summary = "Update discount", description = "Update existing discount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Discount successfully updated"),
            @ApiResponse(responseCode = "404", description = "Discount not found"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    @PutMapping("/{discountId}")
    ResponseEntity<DiscountResponse> updateDiscount(
            @Parameter(description = "Discount ID to update") @PathVariable Long discountId,
            @Parameter(description = "Updated discount data")             @Valid @RequestBody DiscountUpdateRequest request);

    /** Deletes discount by ID. */
    @Operation(summary = "Delete discount", description = "Delete discount by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Discount successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Discount not found")
    })
    @DeleteMapping("/{discountId}")
    ResponseEntity<Void> deleteDiscount(
            @Parameter(description = "Discount ID to delete")             @PathVariable Long discountId);

    /** Returns currently active discounts. */
    @Operation(summary = "Get active discounts", description = "Get list of currently active discounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active discounts successfully retrieved")
    })
    @GetMapping("/active")
    ResponseEntity<List<DiscountResponse>> getActiveDiscounts();

    /** Returns all discounts with pagination. */
    @Operation(summary = "Get all discounts", description = "Get paginated list of all discounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Discounts successfully retrieved")
    })
    @GetMapping
    ResponseEntity<Page<DiscountResponse>> getAllDiscounts(
            @Parameter(description = "Pagination parameters", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""") Pageable pageable);
}
