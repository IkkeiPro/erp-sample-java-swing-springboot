package com.ikkei.swingapp.domain;

public class ProductMasterBean {

    private String productCode;
    private String productName;
    private String updaterCode;

    public ProductMasterBean() {
    }

    public ProductMasterBean(String productCode, String productName, String updaterCode) {
        this.productCode = productCode;
        this.productName = productName;
        this.updaterCode = updaterCode;
    }

    public ProductMasterBean(ProductMasterViewBean source) {
        this(source.getProductCode(), source.getProductName(), source.getUpdaterCode());
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUpdaterCode() {
        return updaterCode;
    }

    public void setUpdaterCode(String updaterCode) {
        this.updaterCode = updaterCode;
    }
}
