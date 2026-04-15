package com.ikkei.swingapp.domain;

public class ProductMasterViewBean extends ProductMasterBean {

    private String updaterName;

    public ProductMasterViewBean() {
    }

    public ProductMasterViewBean(String productCode, String productName, String productCategory, String updaterCode,
            String updaterName) {
        super(productCode, productName, productCategory, updaterCode);
        this.updaterName = updaterName;
    }

    public ProductMasterBean toUpdateBean() {
        return new ProductMasterBean(this);
    }

    public String getUpdaterName() {
        return updaterName;
    }

    public void setUpdaterName(String updaterName) {
        this.updaterName = updaterName;
    }
}
