package com.ikkei.swingapp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ikkei.swingapp.domain.SpecialSpecBean;
import com.ikkei.swingapp.mapper.SpecialSpecMapper;

class SpecialSpecServiceTest {

    private RecordingSpecialSpecMapper mapper;
    private SpecialSpecService service;

    @BeforeEach
    void setUp() {
        mapper = new RecordingSpecialSpecMapper();
        service = new SpecialSpecService(mapper);
    }

    @Test
    void saveAll_acceptsAddChangeDelete() {
        List<SpecialSpecBean> rows = List.of(
                row(1, "A", 1, "X", 9),
                row(2, "B", 2, "C", 3),
                row(3, "D", 4, "Y", 7));

        assertDoesNotThrow(() -> service.saveAll(rows));
        assertEquals(1, mapper.deleteAllCalled);
        assertEquals(3, mapper.insertedRows.size());
        assertNull(rows.get(0).getChangedPartNo());
        assertNull(rows.get(0).getChangedQuantity());
        assertEquals("C", rows.get(1).getChangedPartNo());
        assertEquals(3, rows.get(1).getChangedQuantity());
        assertNull(rows.get(2).getChangedPartNo());
        assertNull(rows.get(2).getChangedQuantity());
    }

    @Test
    void saveAll_rejectsMissingChangedFieldsForOperationChange() {
        List<SpecialSpecBean> rows = List.of(row(2, "B", 2, "", null));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.saveAll(rows));

        assertEquals("1行目: 処理が変更の場合は変更後部品が必須です。", ex.getMessage());
        assertEquals(0, mapper.deleteAllCalled);
        assertEquals(List.of(), mapper.insertedRows);
    }

    private static SpecialSpecBean row(int operation, String partNo, int quantity, String changedPartNo,
            Integer changedQuantity) {
        SpecialSpecBean row = new SpecialSpecBean();
        row.setOperation(operation);
        row.setPartNo(partNo);
        row.setQuantity(quantity);
        row.setChangedPartNo(changedPartNo);
        row.setChangedQuantity(changedQuantity);
        return row;
    }

    private static class RecordingSpecialSpecMapper implements SpecialSpecMapper {
        int deleteAllCalled = 0;
        List<SpecialSpecBean> insertedRows = List.of();

        @Override
        public void createTableIfNotExists() {
        }

        @Override
        public List<SpecialSpecBean> findAll() {
            return List.of();
        }

        @Override
        public void deleteAll() {
            deleteAllCalled++;
        }

        @Override
        public void insertAll(List<SpecialSpecBean> rows) {
            insertedRows = new ArrayList<>(rows);
        }
    }
}
