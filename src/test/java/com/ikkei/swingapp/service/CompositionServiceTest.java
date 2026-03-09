package com.ikkei.swingapp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ikkei.swingapp.domain.CompositionBean;
import com.ikkei.swingapp.mapper.CompositionMapper;

class CompositionServiceTest {

    private RecordingCompositionMapper mapper;
    private CompositionService service;

    @BeforeEach
    void setUp() {
        mapper = new RecordingCompositionMapper();
        service = new CompositionService(mapper);
    }

    @Test
    void saveAll_acceptsValidTree() {
        List<CompositionBean> rows = List.of(
                row("A", "A", 0, 1),
                row("A", "B", 1, 2),
                row("A", "C", 1, 3),
                row("B", "D", 2, 4),
                row("C", "E", 2, 5));

        assertDoesNotThrow(() -> service.saveAll(rows));
        assertEquals(1, mapper.deleteAllCalled);
        assertEquals(rows, mapper.insertedRows);
        assertEquals(1, rows.get(0).getRequiredQuantity());
        assertEquals(2, rows.get(1).getRequiredQuantity());
        assertEquals(3, rows.get(2).getRequiredQuantity());
        assertEquals(8, rows.get(3).getRequiredQuantity());
        assertEquals(15, rows.get(4).getRequiredQuantity());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidTreeCases")
    void saveAll_rejectsInvalidTrees(String scenario, List<CompositionBean> rows, String expectedMessage) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.saveAll(rows));

        assertEquals(expectedMessage, ex.getMessage());
        assertEquals(0, mapper.deleteAllCalled);
        assertEquals(List.of(), mapper.insertedRows);
    }

    static Stream<Arguments> invalidTreeCases() {
        return Stream.of(
                Arguments.of(
                        "レベル未入力",
                        List.of(
                                row("A", "A", null, 1)),
                        "1行目: レベルは必須です。"),
                Arguments.of(
                        "員数未入力",
                        List.of(
                                row("A", "A", 0, null)),
                        "1行目: 員数は必須です。"),
                Arguments.of(
                        "員数が0",
                        List.of(
                                row("A", "A", 0, 0)),
                        "1行目: 員数は1以上で入力してください。"),
                Arguments.of(
                        "レベルが負数",
                        List.of(
                                row("A", "A", -1, 1)),
                        "1行目: レベルは0以上で入力してください。"),
                Arguments.of(
                        "親子部品未入力",
                        List.of(
                                row("A", "A", 0, 1),
                                row("", "B", 1, 1)),
                        "2行目: 親部品・子部品は必須です。"),
                Arguments.of(
                        "レベル0で親子不一致",
                        List.of(
                                row("A", "X", 0, 1),
                                row("A", "B", 1, 1)),
                        "1行目: レベル0は親部品と子部品が同一である必要があります。"),
                Arguments.of(
                        "レベル0の親部品が複数",
                        List.of(
                                row("A", "A", 0, 1),
                                row("B", "B", 0, 1)),
                        "レベル0の親部品は1つに統一してください。"),
                Arguments.of(
                        "レベル1以降で親子同一",
                        List.of(
                                row("A", "A", 0, 1),
                                row("A", "A", 1, 1)),
                        "2行目: レベル1以降は親部品と子部品を同一にできません。"),
                Arguments.of(
                        "ルート件数が1件でない",
                        List.of(
                                row("A", "B", 1, 1)),
                        "レベル0のルート構成は1件だけ登録してください。"),
                Arguments.of(
                        "中間レベル欠落",
                        List.of(
                                row("A", "A", 0, 1),
                                row("A", "B", 2, 1)),
                        "レベル1の構成が欠落しています。連続したツリー構造にしてください。"),
                Arguments.of(
                        "親部品が上位レベルで未定義",
                        List.of(
                                row("A", "A", 0, 1),
                                row("X", "B", 1, 1)),
                        "親部品 X は上位レベルで未定義です。"),
                Arguments.of(
                        "親部品のレベル不一致",
                        List.of(
                                row("A", "A", 0, 1),
                                row("A", "B", 1, 1),
                                row("A", "D", 2, 1)),
                        "親部品 A はレベル1 に存在する必要があります。"),
                Arguments.of(
                        "重複構成",
                        List.of(
                                row("A", "A", 0, 1),
                                row("A", "B", 1, 1),
                                row("A", "B", 1, 1)),
                        "重複した構成が存在します: A -> B"),
                Arguments.of(
                        "子部品に複数親",
                        List.of(
                                row("A", "A", 0, 1),
                                row("A", "B", 1, 1),
                                row("A", "C", 1, 1),
                                row("B", "D", 2, 1),
                                row("C", "D", 2, 1)),
                        "子部品 D に複数の親部品が設定されています。"),
                Arguments.of(
                        "子部品のレベル不整合",
                        List.of(
                                row("A", "A", 0, 1),
                                row("A", "B", 1, 1),
                                row("B", "A", 2, 1)),
                        "子部品 A のレベル定義が不整合です。"));
    }

    private static CompositionBean row(String parent, String child, Integer level, Integer quantity) {
        CompositionBean row = new CompositionBean();
        row.setParentPartNo(parent);
        row.setChildPartNo(child);
        row.setLevel(level);
        row.setQuantity(quantity);
        return row;
    }

    private static class RecordingCompositionMapper implements CompositionMapper {
        int deleteAllCalled = 0;
        List<CompositionBean> insertedRows = List.of();

        @Override
        public void createTableIfNotExists() {
        }

        @Override
        public List<CompositionBean> findAll() {
            return List.of();
        }

        @Override
        public void deleteAll() {
            deleteAllCalled++;
        }

        @Override
        public void insertAll(List<CompositionBean> rows) {
            insertedRows = new ArrayList<>(rows);
        }
    }
}
