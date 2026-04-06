package com.ikkei.swingapp.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ikkei.swingapp.domain.ProductMasterBean;
import com.ikkei.swingapp.domain.ProductMasterViewBean;
import com.ikkei.swingapp.mapper.ProductMasterMapper;

@Service
public class ProductMasterService {

    private final ProductMasterMapper productMasterMapper;

    public ProductMasterService(ProductMasterMapper productMasterMapper) {
        this.productMasterMapper = productMasterMapper;
        this.productMasterMapper.createUpdaterTableIfNotExists();
        this.productMasterMapper.createProductTableIfNotExists();
        this.productMasterMapper.seedUpdaters();
    }

    @Transactional(readOnly = true)
    public List<ProductMasterViewBean> findAllForView() {
        return productMasterMapper.findAllForView();
    }

    @Transactional
    public void saveFromViewBeans(List<ProductMasterViewBean> currentRows, List<ProductMasterViewBean> originalRows) {
        Map<String, ProductMasterViewBean> originalByCode = toCodeMap(originalRows);
        Map<String, ProductMasterViewBean> currentByCode = toCodeMap(currentRows);

        for (Map.Entry<String, ProductMasterViewBean> entry : originalByCode.entrySet()) {
            if (!currentByCode.containsKey(entry.getKey())) {
                productMasterMapper.delete(entry.getValue().toUpdateBean());
            }
        }

        for (Map.Entry<String, ProductMasterViewBean> entry : currentByCode.entrySet()) {
            ProductMasterViewBean current = entry.getValue();
            ProductMasterViewBean original = originalByCode.get(entry.getKey());
            ProductMasterBean updateBean = current.toUpdateBean();

            if (original == null) {
                productMasterMapper.insert(updateBean);
                continue;
            }

            if (isChanged(current, original)) {
                productMasterMapper.update(updateBean);
            }
        }
    }

    private boolean isChanged(ProductMasterViewBean current, ProductMasterViewBean original) {
        return !Objects.equals(current.getProductName(), original.getProductName())
                || !Objects.equals(current.getUpdaterCode(), original.getUpdaterCode());
    }

    private Map<String, ProductMasterViewBean> toCodeMap(List<ProductMasterViewBean> rows) {
        Map<String, ProductMasterViewBean> byCode = new LinkedHashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            ProductMasterViewBean row = rows.get(i);
            String code = trimToNull(row.getProductCode());
            String name = trimToNull(row.getProductName());
            String updaterCode = trimToNull(row.getUpdaterCode());

            if (code == null || name == null || updaterCode == null) {
                throw new IllegalArgumentException((i + 1) + "行目: 商品コード・商品名・更新担当者コードは必須です。");
            }
            row.setProductCode(code);
            row.setProductName(name);
            row.setUpdaterCode(updaterCode);

            if (byCode.putIfAbsent(code, row) != null) {
                throw new IllegalArgumentException((i + 1) + "行目: 商品コードが重複しています。コード=" + code);
            }
        }
        return byCode;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
