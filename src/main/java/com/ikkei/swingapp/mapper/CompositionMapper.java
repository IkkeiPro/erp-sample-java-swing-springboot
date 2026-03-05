package com.ikkei.swingapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ikkei.swingapp.domain.CompositionBean;

@Mapper
public interface CompositionMapper {

    void createTableIfNotExists();

    List<CompositionBean> findAll();

    void deleteAll();

    void insertAll(@Param("rows") List<CompositionBean> rows);
}
