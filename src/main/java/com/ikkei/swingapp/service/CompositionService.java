package com.ikkei.swingapp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ikkei.swingapp.domain.CompositionBean;
import com.ikkei.swingapp.mapper.CompositionMapper;

@Service
public class CompositionService {

    private final CompositionMapper compositionMapper;

    public CompositionService(CompositionMapper compositionMapper) {
        this.compositionMapper = compositionMapper;
    }

    @Transactional(readOnly = true)
    public List<CompositionBean> findAll() {
        return compositionMapper.findAll();
    }

    @Transactional
    public void saveAll(List<CompositionBean> rows) {
        compositionMapper.deleteAll();

        if (!rows.isEmpty()) {
            compositionMapper.insertAll(rows);
        }
    }
}
