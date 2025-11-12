package ru.ifmo.highload.impl.actual_price;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("actual_price")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ActualPrice {

    @Id
    private Long id;

    @Column("product_id")
    private Long productId;

    @Column("price")
    private Integer price;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
