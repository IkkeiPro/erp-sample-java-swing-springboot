package com.ikkei.swingapp.domain;

public class CompositionRow {

    private String parentPartNo;
    private String childPartNo;
    private Integer level;

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
}
