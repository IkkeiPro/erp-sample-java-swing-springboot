package com.ikkei.swingapp.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        validateAndCalculateRequiredQuantity(rows);
        compositionMapper.deleteAll();

        if (!rows.isEmpty()) {
            compositionMapper.insertAll(rows);
        }
    }

    public void validateAndCalculateRequiredQuantity(List<CompositionBean> rows) {
        sortByLevel(rows);
        validateTree(rows);
        calculateRequiredQuantity(rows);
    }

    private void sortByLevel(List<CompositionBean> rows) {
        rows.sort(Comparator
                .comparing(CompositionBean::getLevel)
                .thenComparing(CompositionBean::getParentPartNo)
                .thenComparing(CompositionBean::getChildPartNo));
    }

    private void calculateRequiredQuantity(List<CompositionBean> rows) {
        Map<Integer, List<CompositionBean>> rowsByLevel = new HashMap<>();
        for (CompositionBean row : rows) {
            rowsByLevel.computeIfAbsent(row.getLevel(), ignored -> new ArrayList<>()).add(row);
        }

        Map<String, Integer> requiredByPartNo = new HashMap<>();
        List<CompositionBean> levelZeroRows = rowsByLevel.get(0);
        if (levelZeroRows == null || levelZeroRows.isEmpty()) {
            return;
        }

        CompositionBean root = levelZeroRows.get(0);
        root.setRequiredQuantity(root.getQuantity());
        requiredByPartNo.put(root.getChildPartNo(), root.getRequiredQuantity());

        int level = 1;
        while (rowsByLevel.containsKey(level)) {
            for (CompositionBean row : rowsByLevel.get(level)) {
                Integer parentRequiredQuantity = requiredByPartNo.get(row.getParentPartNo());
                if (parentRequiredQuantity == null) {
                    throw new IllegalArgumentException("親部品 " + row.getParentPartNo() + " の所要量が計算できません。");
                }

                int requiredQuantity = parentRequiredQuantity * row.getQuantity();
                row.setRequiredQuantity(requiredQuantity);
                requiredByPartNo.put(row.getChildPartNo(), requiredQuantity);
            }
            level++;
        }
    }

    private void validateTree(List<CompositionBean> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        Map<Integer, List<CompositionBean>> rowsByLevel = new HashMap<>();
        int maxLevel = 0;
        int rootCount = 0;
        String rootPartNo = null;

        for (int i = 0; i < rows.size(); i++) {
            CompositionBean row = rows.get(i);
            int lineNo = i + 1;

            if (row.getLevel() == null) {
                throw new IllegalArgumentException(lineNo + "行目: レベルは必須です。");
            }
            if (row.getLevel() < 0) {
                throw new IllegalArgumentException(lineNo + "行目: レベルは0以上で入力してください。");
            }

            String parentPartNo = row.getParentPartNo();
            String childPartNo = row.getChildPartNo();

            if (row.getQuantity() == null) {
                throw new IllegalArgumentException(lineNo + "行目: 員数は必須です。");
            }
            if (row.getQuantity() <= 0) {
                throw new IllegalArgumentException(lineNo + "行目: 員数は1以上で入力してください。");
            }

            if (parentPartNo == null || parentPartNo.isBlank() || childPartNo == null || childPartNo.isBlank()) {
                throw new IllegalArgumentException(lineNo + "行目: 親部品・子部品は必須です。");
            }

            rowsByLevel.computeIfAbsent(row.getLevel(), ignored -> new ArrayList<>()).add(row);

            maxLevel = Math.max(maxLevel, row.getLevel());

            if (row.getLevel() == 0) {
                rootCount++;
                if (!parentPartNo.equals(childPartNo)) {
                    throw new IllegalArgumentException(lineNo + "行目: レベル0は親部品と子部品が同一である必要があります。");
                }
                if (rootPartNo == null) {
                    rootPartNo = parentPartNo;
                } else if (!rootPartNo.equals(parentPartNo)) {
                    throw new IllegalArgumentException("レベル0の親部品は1つに統一してください。");
                }
            } else if (parentPartNo.equals(childPartNo)) {
                throw new IllegalArgumentException(lineNo + "行目: レベル1以降は親部品と子部品を同一にできません。");
            }
        }

        if (rootCount != 1) {
            throw new IllegalArgumentException("レベル0のルート構成は1件だけ登録してください。");
        }

        Map<String, Integer> partLevel = new HashMap<>();
        Map<String, String> parentByChild = new HashMap<>();
        Set<String> edgeSet = new HashSet<>();
        partLevel.put(rootPartNo, 0);

        for (int level = 1; level <= maxLevel; level++) {
            List<CompositionBean> currentLevelRows = rowsByLevel.get(level);
            if (currentLevelRows == null || currentLevelRows.isEmpty()) {
                throw new IllegalArgumentException("レベル" + level + "の構成が欠落しています。連続したツリー構造にしてください。");
            }

            for (CompositionBean row : currentLevelRows) {
                String parentPartNo = row.getParentPartNo();
                String childPartNo = row.getChildPartNo();

                Integer parentLevel = partLevel.get(parentPartNo);
                if (parentLevel == null) {
                    throw new IllegalArgumentException("親部品 " + parentPartNo + " は上位レベルで未定義です。");
                }
                if (parentLevel != level - 1) {
                    throw new IllegalArgumentException(
                            "親部品 " + parentPartNo + " はレベル" + (level - 1) + " に存在する必要があります。");
                }

                String edgeKey = parentPartNo + "->" + childPartNo;
                if (!edgeSet.add(edgeKey)) {
                    throw new IllegalArgumentException("重複した構成が存在します: " + parentPartNo + " -> " + childPartNo);
                }

                String existingParent = parentByChild.get(childPartNo);
                if (existingParent == null) {
                    parentByChild.put(childPartNo, parentPartNo);
                } else if (!existingParent.equals(parentPartNo)) {
                    throw new IllegalArgumentException("子部品 " + childPartNo + " に複数の親部品が設定されています。");
                }

                Integer existingLevel = partLevel.get(childPartNo);
                if (existingLevel != null && existingLevel != level) {
                    throw new IllegalArgumentException("子部品 " + childPartNo + " のレベル定義が不整合です。");
                }
                partLevel.put(childPartNo, level);
            }
        }
    }
}
