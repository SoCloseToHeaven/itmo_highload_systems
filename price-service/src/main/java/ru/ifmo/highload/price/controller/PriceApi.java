package ru.ifmo.highload.price.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;
import ru.ifmo.highload.price.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.price.dto.actual_price.PriceResponse;
import ru.ifmo.highload.price.dto.actual_price.PriceUpdateRequest;

/**
 * Price management API for administrators
 */
@Tag(name = "Price Management", description = "API for managing product prices (for administrators)")
public interface PriceApi {

    /**
     * Create a new price for a product
     */
    @Operation(summary = "Create price", description = "Create a new price for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Price successfully created"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Price for this product already exists")
    })
    @PostMapping
    Mono<ResponseEntity<PriceResponse>> createPrice(
            @Parameter(description = "Price creation data") @Valid @RequestBody PriceCreateRequest request);

    /**
     * Update existing price by its ID
     */
    @Operation(summary = "Update price by ID", description = "Update existing price by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price successfully updated"),
            @ApiResponse(responseCode = "404", description = "Price not found")
    })
    @PutMapping("/{priceId}")
    Mono<ResponseEntity<PriceResponse>> updatePrice(
            @Parameter(description = "Price ID to update") @PathVariable Long priceId,
            @Parameter(description = "Updated price data") @Valid @RequestBody PriceUpdateRequest request);

    /**
     * Delete price by its ID
     */
    @Operation(summary = "Delete price by ID", description = "Delete price by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Price successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Price not found")
    })
    @DeleteMapping("/{priceId}")
    Mono<ResponseEntity<Void>> deletePrice(
            @Parameter(description = "Price ID to delete") @PathVariable Long priceId);

    /**
     * Update price for a specific product
     */
    @Operation(summary = "Update price by product ID", description = "Update price for a specific product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price successfully updated"),
            @ApiResponse(responseCode = "404", description = "Price for this product not found")
    })
    @PutMapping("/product/{productId}")
    Mono<ResponseEntity<PriceResponse>> updatePriceByProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Updated price data") @Valid @RequestBody PriceUpdateRequest request);

    /**
     * Delete price for a specific product
     */
    @Operation(summary = "Delete price by product ID", description = "Delete price for a specific product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Price successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Price for this product not found")
    })
    @DeleteMapping("/product/{productId}")
    Mono<ResponseEntity<Void>> deletePriceByProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId);

    /**
     * Get current price for a specific product
     */
    @Operation(summary = "Get current price by product ID", description = "Get current price for a specific product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Price for this product not found")
    })
    @GetMapping("/product/{productId}/current")
    Mono<ResponseEntity<Integer>> getCurrentPriceForProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId);

    /**
     * Get paginated list of all prices
     */
    @Operation(summary = "Get all prices", description = "Get paginated list of all prices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prices successfully retrieved")
    })
    @GetMapping
    Mono<ResponseEntity<Page<PriceResponse>>> getAllPrices(
            @Parameter(description = "Pagination parameters", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""") Pageable pageable);
}
