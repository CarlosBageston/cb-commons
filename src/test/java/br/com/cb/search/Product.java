package br.com.cb.search;

import br.com.cb.baseEntity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Product extends BaseEntity {

    public enum ItemType { FINISHED_PRODUCT, INTERMEDIATE_PRODUCT, RAW_MATERIAL }

    public String name;
    public String internalCode;

    @Enumerated(EnumType.STRING)
    public ItemType itemType;

    public Boolean active;
    public Integer price;

    public Product() {}

    public Product(String name, String internalCode, ItemType itemType, boolean active, int price) {
        this.name = name;
        this.internalCode = internalCode;
        this.itemType = itemType;
        this.active = active;
        this.price = price;
    }
}
