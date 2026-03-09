package com.ikkei.swingapp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ikkei.swingapp.domain.SpecialSpecBean;
import com.ikkei.swingapp.mapper.SpecialSpecMapper;

@Service
public class SpecialSpecService {

    public static final int OPERATION_ADD = 1;
    public static final int OPERATION_CHANGE = 2;
    public static final int OPERATION_DELETE = 3;

    private final SpecialSpecMapper specialSpecMapper;

    public SpecialSpecService(SpecialSpecMapper specialSpecMapper) {
        this.specialSpecMapper = specialSpecMapper;
        this.specialSpecMapper.createTableIfNotExists();
    }

    @Transactional(readOnly = true)
    public List<SpecialSpecBean> findAll() {
        return specialSpecMapper.findAll();
    }

    @Transactional
    public void saveAll(List<SpecialSpecBean> rows) {
        validate(rows);
        specialSpecMapper.deleteAll();
        if (!rows.isEmpty()) {
            specialSpecMapper.insertAll(rows);
        }
    }

    private void validate(List<SpecialSpecBean> rows) {
        for (int i = 0; i < rows.size(); i++) {
            SpecialSpecBean row = rows.get(i);
            int lineNo = i + 1;

            if (row.getOperation() == null) {
                throw new IllegalArgumentException(lineNo + "行目: 処理は必須です。");
            }
            if (row.getOperation() != OPERATION_ADD
                    && row.getOperation() != OPERATION_CHANGE
                    && row.getOperation() != OPERATION_DELETE) {
                throw new IllegalArgumentException(lineNo + "行目: 処理は1(追加),2(変更),3(削除)のいずれかを選択してください。");
            }
            if (row.getPartNo() == null || row.getPartNo().isBlank()) {
                throw new IllegalArgumentException(lineNo + "行目: 部品は必須です。");
            }
            if (row.getQuantity() == null || row.getQuantity() <= 0) {
                throw new IllegalArgumentException(lineNo + "行目: 員数は1以上で入力してください。");
            }

            if (row.getOperation() == OPERATION_CHANGE) {
                if (row.getChangedPartNo() == null || row.getChangedPartNo().isBlank()) {
                    throw new IllegalArgumentException(lineNo + "行目: 処理が変更の場合は変更後部品が必須です。");
                }
                if (row.getChangedQuantity() == null || row.getChangedQuantity() <= 0) {
                    throw new IllegalArgumentException(lineNo + "行目: 処理が変更の場合は変更後員数を1以上で入力してください。");
                }
            } else {
                row.setChangedPartNo(null);
                row.setChangedQuantity(null);
            }
        }
    }
}
