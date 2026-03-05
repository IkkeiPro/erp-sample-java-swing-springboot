package com.ikkei.swingapp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ikkei.swingapp.domain.CompositionRow;
import com.ikkei.swingapp.mapper.CompositionMapper;

@Service
public class CompositionService {

    private final CompositionMapper compositionMapper;

    public CompositionService(CompositionMapper compositionMapper) {
        this.compositionMapper = compositionMapper;
    }

    @Transactional(readOnly = true)
    public List<CompositionRow> findAll() {
        compositionMapper.createTableIfNotExists();
        return compositionMapper.findAll();
    }

    @Transactional
    public void saveAll(List<CompositionRow> rows) {
        compositionMapper.createTableIfNotExists();
        compositionMapper.deleteAll();

        if (!rows.isEmpty()) {
            compositionMapper.insertAll(rows);
        }
    }
}
