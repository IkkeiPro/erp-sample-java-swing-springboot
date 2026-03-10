package com.ikkei.swingapp.domain;

public class SpecialSpecBean {

    private Integer operation;
    private String partNo;
    private Integer quantity;
    private String changedPartNo;
    private Integer changedQuantity;
    private Integer requiredQuantity;

    public Integer getOperation() {
        return operation;
    }

    public void setOperation(Integer operation) {
        this.operation = operation;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getChangedPartNo() {
        return changedPartNo;
    }

    public void setChangedPartNo(String changedPartNo) {
        this.changedPartNo = changedPartNo;
    }

    public Integer getChangedQuantity() {
        return changedQuantity;
    }

    public void setChangedQuantity(Integer changedQuantity) {
        this.changedQuantity = changedQuantity;
    }

    public Integer getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(Integer requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }
}
