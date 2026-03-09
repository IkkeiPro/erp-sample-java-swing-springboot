package com.ikkei.swingapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ikkei.swingapp.domain.SpecialSpecBean;

@Mapper
public interface SpecialSpecMapper {

    void createTableIfNotExists();

    List<SpecialSpecBean> findAll();

    void deleteAll();

    void insertAll(@Param("rows") List<SpecialSpecBean> rows);
}
