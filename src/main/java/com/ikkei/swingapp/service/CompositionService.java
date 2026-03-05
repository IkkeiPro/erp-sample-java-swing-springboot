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
import java.util.ArrayList;

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

        // 各レベルの構成一覧
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
                throw new IllegalArgumentException(lineNo + "行目: 親ファイル・子ファイルは必須です。");
            }

            // Map変数rowsByLevelの中からrowと同レベルのリストを参照
            List<CompositionBean> list = rowsByLevel.get(row.getLevel());
            // rowと同レベルのリストがない場合、rowsByLevelに新しいリスト追加
            if (list == null) {
                list = new ArrayList<>();
                rowsByLevel.put(row.getLevel(), list);
            }
            // rowをrowsByLevelの中のリストに追加
            list.add(row);
            
            maxLevel = Math.max(maxLevel, row.getLevel());

            if (row.getLevel() == 0) {
                rootCount++;
                if (!parentPartNo.equals(childPartNo)) {
                    throw new IllegalArgumentException(lineNo + "行目: レベル0は親ファイルと子ファイルが同一である必要があります。");
                }
                if (rootPartNo == null) {
                    rootPartNo = parentPartNo;
                } else if (!rootPartNo.equals(parentPartNo)) {
                    throw new IllegalArgumentException("レベル0の親ファイルは1つに統一してください。");
                }
            } else if (parentPartNo.equals(childPartNo)) {
                throw new IllegalArgumentException(lineNo + "行目: レベル1以降は親ファイルと子ファイルを同一にできません。");
            }
        }

        if (rootCount != 1) {
            throw new IllegalArgumentException("レベル0のルート構成は1件だけ登録してください。");
        }

        // ファイルのレベル辞書
        Map<String, Integer> partLevel = new HashMap<>();
        // 親子の組合せ辞書
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
                // 親ファイルが上位に存在するかチェック
                if (parentLevel == null) {
                    throw new IllegalArgumentException("親ファイル " + parentPartNo + " は上位レベルで未定義です。");
                }
                // 親ファイルが子ファイルの1レベル上にいるかチェック
                if (parentLevel != level - 1) {
                    throw new IllegalArgumentException(
                            "親ファイル " + parentPartNo + " はレベル" + (level - 1) + " に存在する必要があります。");
                }

                // 重複禁止のコレクションに親ファイル・子ファイルの組合せを追加して、失敗ならエラー
                String edgeKey = parentPartNo + "->" + childPartNo;
                if (!edgeSet.add(edgeKey)) {
                    throw new IllegalArgumentException("重複した構成が存在します: " + parentPartNo + " -> " + childPartNo);
                }

                // 既に登録済のchildPartNoの親を取得（なければnull）
                String existingParent = parentByChild.get(childPartNo);
                if (existingParent == null) {
                    // childPartNoの親が未登録、今回の親を登録する
                    parentByChild.put(childPartNo, parentPartNo);
                } else if (!existingParent.equals(parentPartNo)) {
                    // childPartNoに複数種類の親がいるからエラー
                    throw new IllegalArgumentException("子ファイル " + childPartNo + " に複数の親ファイルが設定されています。");
                }

                // 今までに出てきたchildPartNoのレベルを取得。未出ならnull
                Integer existingLevel = partLevel.get(childPartNo);
                // childPartNoが別のレベルで既出ならエラー
                if (existingLevel != null && existingLevel != level) {
                    throw new IllegalArgumentException("子ファイル " + childPartNo + " のレベル定義が不整合です。");
                }
                // ファイルのレベル辞書を更新
                partLevel.put(childPartNo, level);
            }
        }
    }
}
