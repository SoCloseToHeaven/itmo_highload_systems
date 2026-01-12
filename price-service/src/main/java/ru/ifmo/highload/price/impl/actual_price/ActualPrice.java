package ru.ifmo.highload.price.impl.actual_price;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Table("actual_price")
@Getter
@Setter
class ActualPrice {

    @Id
    private Long id;

    @Column("product_id")
    private Long productId;

    @Column("price")
    private Integer price;

    @Column("created_at")
    private ZonedDateTime createdAt;

    @Column("updated_at")
    private ZonedDateTime updatedAt;
}

