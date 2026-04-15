package com.ikkei.swingapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ikkei.swingapp.domain.ProductMasterBean;
import com.ikkei.swingapp.domain.ProductMasterViewBean;

@Mapper
public interface ProductMasterMapper {

    void createUpdaterTableIfNotExists();

    void createProductTableIfNotExists();

    void addProductCategoryColumnIfNotExists();

    void seedUpdaters();

    List<ProductMasterViewBean> findAllForView();

    void insert(ProductMasterBean row);

    void update(ProductMasterBean row);

    void delete(ProductMasterBean row);
}
