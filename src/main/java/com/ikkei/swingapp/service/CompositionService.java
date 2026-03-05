package com.ikkei.swingapp.service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
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
        validateTree(rows);
        compositionMapper.deleteAll();

        if (!rows.isEmpty()) {
            compositionMapper.insertAll(rows);
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

            if (parentPartNo == null || parentPartNo.isBlank() || childPartNo == null || childPartNo.isBlank()) {
                throw new IllegalArgumentException(lineNo + "行目: 親品番・子品番は必須です。");
            }

            int level = row.getLevel();
            rowsByLevel.computeIfAbsent(level, key -> new java.util.ArrayList<>()).add(row);
            maxLevel = Math.max(maxLevel, level);

            if (level == 0) {
                rootCount++;
                if (!parentPartNo.equals(childPartNo)) {
                    throw new IllegalArgumentException(lineNo + "行目: レベル0は親品番と子品番が同一である必要があります。");
                }
                if (rootPartNo == null) {
                    rootPartNo = parentPartNo;
                } else if (!rootPartNo.equals(parentPartNo)) {
                    throw new IllegalArgumentException("レベル0の親品番は1つに統一してください。");
                }
            } else if (parentPartNo.equals(childPartNo)) {
                throw new IllegalArgumentException(lineNo + "行目: レベル1以降は親品番と子品番を同一にできません。");
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
                    throw new IllegalArgumentException("親品番 " + parentPartNo + " は上位レベルで未定義です。");
                }
                if (parentLevel != level - 1) {
                    throw new IllegalArgumentException(
                            "親品番 " + parentPartNo + " はレベル" + (level - 1) + " に存在する必要があります。");
                }

                String edgeKey = parentPartNo + "->" + childPartNo;
                if (!edgeSet.add(edgeKey)) {
                    throw new IllegalArgumentException("重複した構成が存在します: " + parentPartNo + " -> " + childPartNo);
                }

                String existingParent = parentByChild.putIfAbsent(childPartNo, parentPartNo);
                if (existingParent != null && !existingParent.equals(parentPartNo)) {
                    throw new IllegalArgumentException("子品番 " + childPartNo + " に複数の親品番が設定されています。");
                }

                Integer existingLevel = partLevel.get(childPartNo);
                if (existingLevel != null && existingLevel != level) {
                    throw new IllegalArgumentException("子品番 " + childPartNo + " のレベル定義が不整合です。");
                }
                partLevel.put(childPartNo, level);
            }
        }
    }
}
