package com.ikkei.swingapp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ikkei.swingapp.domain.CompositionBean;
import com.ikkei.swingapp.domain.SpecialSpecBean;
import com.ikkei.swingapp.mapper.SpecialSpecMapper;

@Service
public class SpecialSpecService {

    public static final int OPERATION_ADD = 1;
    public static final int OPERATION_CHANGE = 2;
    public static final int OPERATION_DELETE = 3;

    private final SpecialSpecMapper specialSpecMapper;
    private final CompositionService compositionService;

    public SpecialSpecService(SpecialSpecMapper specialSpecMapper, CompositionService compositionService) {
        this.specialSpecMapper = specialSpecMapper;
        this.compositionService = compositionService;
        this.specialSpecMapper.createTableIfNotExists();
        this.specialSpecMapper.addRequiredQuantityColumnIfNotExists();
    }

    @Transactional(readOnly = true)
    public List<SpecialSpecBean> findAll() {
        return specialSpecMapper.findAll();
    }

    @Transactional
    public void saveAll(List<SpecialSpecBean> rows) {
        validate(rows);
        calculateRequiredQuantity(rows);
        specialSpecMapper.deleteAll();
        if (!rows.isEmpty()) {
            specialSpecMapper.insertAll(rows);
        }
    }

    private void calculateRequiredQuantity(List<SpecialSpecBean> rows) {
        List<CompositionBean> baseRows = deepCopy(compositionService.findAll());
        List<CompositionBean> appliedRows = applySpecialSpecs(baseRows, rows);
        compositionService.validateAndCalculateRequiredQuantity(appliedRows);

        for (SpecialSpecBean row : rows) {
            row.setRequiredQuantity(findRequiredQuantity(appliedRows, row));
        }
    }

    private List<CompositionBean> applySpecialSpecs(List<CompositionBean> compositionRows, List<SpecialSpecBean> specRows) {
        if (compositionRows.isEmpty()) {
            return compositionRows;
        }

        CompositionBean root = compositionRows.stream()
                .filter(r -> Integer.valueOf(0).equals(r.getLevel()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("BOMのルート構成が存在しません。"));

        for (SpecialSpecBean spec : specRows) {
            if (spec.getOperation() == OPERATION_ADD) {
                CompositionBean added = new CompositionBean();
                added.setParentPartNo(root.getChildPartNo());
                added.setChildPartNo(spec.getPartNo());
                added.setLevel(1);
                added.setQuantity(spec.getQuantity());
                compositionRows.add(added);
            } else if (spec.getOperation() == OPERATION_CHANGE) {
                for (CompositionBean row : compositionRows) {
                    if (row.getChildPartNo().equals(spec.getPartNo())  
                        && row.getQuantity().equals(spec.getQuantity())) {
                        row.setChildPartNo(spec.getChangedPartNo());
                        row.setQuantity(spec.getChangedQuantity());
                    }
                }
            } else if (spec.getOperation() == OPERATION_DELETE) {
                compositionRows.removeIf(row -> matches(row, spec.getPartNo(), spec.getQuantity()));
            }
        }

        return compositionRows;
    }

    private int findRequiredQuantity(List<CompositionBean> appliedRows, SpecialSpecBean spec) {
        if (spec.getOperation() == OPERATION_CHANGE) {
            return sumRequiredQuantity(appliedRows, spec.getChangedPartNo(), spec.getChangedQuantity());
        }
        return sumRequiredQuantity(appliedRows, spec.getPartNo(), spec.getQuantity());
    }

    private int sumRequiredQuantity(List<CompositionBean> rows, String partNo, Integer quantity) {
        int sum = 0;
        for (CompositionBean row : rows) {
            if (matches(row, partNo, quantity)) {
                sum += row.getRequiredQuantity();
            }
        }
        return sum;
    }

    private boolean matches(CompositionBean row, String partNo, Integer quantity) {
        return row.getChildPartNo().equals(partNo) && row.getQuantity().equals(quantity);
    }

    private List<CompositionBean> deepCopy(List<CompositionBean> rows) {
        List<CompositionBean> copied = new ArrayList<>();
        for (CompositionBean row : rows) {
            CompositionBean clone = new CompositionBean();
            clone.setParentPartNo(row.getParentPartNo());
            clone.setChildPartNo(row.getChildPartNo());
            clone.setLevel(row.getLevel());
            clone.setQuantity(row.getQuantity());
            clone.setRequiredQuantity(row.getRequiredQuantity());
            copied.add(clone);
        }
        return copied;
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
