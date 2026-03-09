package com.ikkei.swingapp.domain;

public class CompositionBean {

    private String parentPartNo;
    private String childPartNo;
    private Integer level;
    private Integer quantity;
    private Integer requiredQuantity;

    public String getParentPartNo() {
        return parentPartNo;
    }

    public void setParentPartNo(String parentPartNo) {
        this.parentPartNo = parentPartNo;
    }

    public String getChildPartNo() {
        return childPartNo;
    }

    public void setChildPartNo(String childPartNo) {
        this.childPartNo = childPartNo;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(Integer requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }
}
