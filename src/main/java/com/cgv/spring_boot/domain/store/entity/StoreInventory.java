package com.cgv.spring_boot.domain.store.entity;

import com.cgv.spring_boot.domain.theater.entity.Theater;
import com.cgv.spring_boot.domain.store.exception.StoreErrorCode;
import com.cgv.spring_boot.global.common.entity.BaseEntity;
import com.cgv.spring_boot.global.error.exception.BusinessException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx_store_inventory_theater_item", columnList = "theater_id, item_id")
})
public class StoreInventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id")
    private Theater theater;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Min(value = 1, message = "재고는 최소 1개 이상이어야 합니다.")
    @Column(nullable = false)
    private int stock; // 재고 수량

    @Builder
    public StoreInventory(Theater theater, Item item, int stock) {
        validateStock(stock);
        this.theater = theater;
        this.item = item;
        this.stock = stock;
    }

    public void decreaseStock(int quantity) {
        if (quantity < 1) {
            throw new BusinessException(StoreErrorCode.INVALID_STOCK_QUANTITY);
        }
        if (this.stock - quantity < 1) {
            throw new BusinessException(StoreErrorCode.INSUFFICIENT_STOCK);
        }
        this.stock -= quantity;
    }

    private void validateStock(int stock) {
        if (stock < 1) {
            throw new BusinessException(StoreErrorCode.INVALID_STOCK_QUANTITY);
        }
    }
}
