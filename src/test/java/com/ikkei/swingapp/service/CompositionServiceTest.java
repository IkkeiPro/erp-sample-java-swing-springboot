package com.ikkei.swingapp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
                row("A", "A", 0),
                row("A", "B", 1),
                row("A", "C", 1),
                row("B", "D", 2),
                row("C", "E", 2));

        assertDoesNotThrow(() -> service.saveAll(rows));
        assertEquals(1, mapper.deleteAllCalled);
        assertEquals(rows, mapper.insertedRows);
    }

    @Test
    void saveAll_rejectsWhenRootIsInvalid() {
        List<CompositionBean> rows = List.of(
                row("A", "X", 0),
                row("A", "B", 1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.saveAll(rows));
        assertEquals("1行目: レベル0は親品番と子品番が同一である必要があります。", ex.getMessage());
        assertEquals(0, mapper.deleteAllCalled);
    }

    @Test
    void saveAll_rejectsWhenChildHasMultipleParents() {
        List<CompositionBean> rows = List.of(
                row("A", "A", 0),
                row("A", "B", 1),
                row("A", "C", 1),
                row("B", "D", 2),
                row("C", "D", 2));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.saveAll(rows));
        assertEquals("子品番 D に複数の親品番が設定されています。", ex.getMessage());
        assertEquals(0, mapper.deleteAllCalled);
    }

    @Test
    void saveAll_rejectsWhenParentLevelMismatch() {
        List<CompositionBean> rows = List.of(
                row("A", "A", 0),
                row("A", "B", 1),
                row("A", "D", 2));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.saveAll(rows));
        assertEquals("親品番 A はレベル1 に存在する必要があります。", ex.getMessage());
        assertEquals(0, mapper.deleteAllCalled);
    }

    private static CompositionBean row(String parent, String child, int level) {
        CompositionBean row = new CompositionBean();
        row.setParentPartNo(parent);
        row.setChildPartNo(child);
        row.setLevel(level);
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
