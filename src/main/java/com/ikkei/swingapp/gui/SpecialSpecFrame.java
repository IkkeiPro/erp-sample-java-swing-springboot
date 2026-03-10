package com.ikkei.swingapp.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.springframework.stereotype.Component;

import com.ikkei.swingapp.domain.SpecialSpecBean;
import com.ikkei.swingapp.service.SpecialSpecService;

@Component
public class SpecialSpecFrame extends JFrame {

    private static final String[] COLUMNS = {"処理", "部品", "員数", "変更後部品", "変更後員数", "所要量"};

    private final SpecialSpecService specialSpecService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final Deque<DeletedRow> deletedRows = new ArrayDeque<>();
    private boolean suppressModelListener = false;

    public SpecialSpecFrame(SpecialSpecService specialSpecService) {
        super("特別仕様画面");
        this.specialSpecService = specialSpecService;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(860, 420);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 5) {
                    return false;
                }
                if (column == 0 || column == 1 || column == 2) {
                    return true;
                }
                int operation = operationFromCell(row);
                if (operation == SpecialSpecService.OPERATION_CHANGE) {
                    return column == 3 || column == 4;
                }
                return false;
            }
        };

        table = new JTable(tableModel);
        JComboBox<String> operationCombo = new JComboBox<>(new String[] {"1.追加", "2.変更", "3.削除"});
        table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(operationCombo));
        tableModel.addTableModelListener(e -> clearChangedColumnsWhenNeeded());

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addButton = new JButton("行追加");
        addButton.addActionListener(e -> tableModel.addRow(new Object[] {"1.追加", "", 1, "", null, null}));

        JButton deleteButton = new JButton("削除");
        deleteButton.addActionListener(e -> deleteSelectedRows());

        JButton undoDeleteButton = new JButton("削除取り消し");
        undoDeleteButton.addActionListener(e -> undoDelete());

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> saveTable());

        JButton closeButton = new JButton("閉じる");
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(undoDeleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        reloadTable();
    }

    public void reloadTable() {
        deletedRows.clear();
        tableModel.setRowCount(0);
        List<SpecialSpecBean> rows = specialSpecService.findAll();
        for (SpecialSpecBean row : rows) {
            tableModel.addRow(new Object[] {
                    operationLabel(row.getOperation()),
                    row.getPartNo(),
                    row.getQuantity(),
                    row.getChangedPartNo() == null ? "" : row.getChangedPartNo(),
                    row.getChangedQuantity(),
                    row.getRequiredQuantity()});
        }
    }

    private void saveTable() {
        try {
            List<SpecialSpecBean> rows = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                SpecialSpecBean row = new SpecialSpecBean();
                row.setOperation(parseOperation(tableModel.getValueAt(i, 0)));
                row.setPartNo(String.valueOf(tableModel.getValueAt(i, 1)).trim());
                row.setQuantity(Integer.parseInt(String.valueOf(tableModel.getValueAt(i, 2)).trim()));

                String changedPartNo = String.valueOf(tableModel.getValueAt(i, 3)).trim();
                String changedQuantityRaw = String.valueOf(tableModel.getValueAt(i, 4)).trim();
                if(row.getOperation() == 2) {
                    row.setChangedPartNo(changedPartNo.isEmpty() ? null : changedPartNo);
                    row.setChangedQuantity(changedQuantityRaw.isEmpty() ? null : Integer.parseInt(changedQuantityRaw));
                } else {
                    row.setChangedPartNo(null);
                    row.setChangedQuantity(0);
                }
                row.setRequiredQuantity(0);
                rows.add(row);
            }

            specialSpecService.saveAll(rows);
            reloadTable();
            deletedRows.clear();
            JOptionPane.showMessageDialog(this, "特別仕様マスターを保存しました。", "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "員数・変更後員数は数値で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "保存エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedRows() {
        int[] selected = table.getSelectedRows();
        if (selected.length == 0) {
            return;
        }

        for (int i = selected.length - 1; i >= 0; i--) {
            int rowIndex = selected[i];
            Object[] rowData = {
                    tableModel.getValueAt(rowIndex, 0),
                    tableModel.getValueAt(rowIndex, 1),
                    tableModel.getValueAt(rowIndex, 2),
                    tableModel.getValueAt(rowIndex, 3),
                    tableModel.getValueAt(rowIndex, 4),
                    tableModel.getValueAt(rowIndex, 5)};
            deletedRows.push(new DeletedRow(rowIndex, rowData));
            tableModel.removeRow(rowIndex);
        }
    }

    private void undoDelete() {
        if (deletedRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "取り消せる削除がありません。", "情報", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DeletedRow deleted = deletedRows.pop();
        int rowIndex = Math.min(deleted.rowIndex(), tableModel.getRowCount());
        tableModel.insertRow(rowIndex, deleted.rowData());
    }

    private void clearChangedColumnsWhenNeeded() {
        if (suppressModelListener) {
            return;
        }

        suppressModelListener = true;
        try {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int operation = operationFromCell(i);
                if (operation != SpecialSpecService.OPERATION_CHANGE) {
                    tableModel.setValueAt("", i, 3);
                    tableModel.setValueAt(null, i, 4);
                    tableModel.setValueAt(null, i, 5);
                }
            }
        } finally {
            suppressModelListener = false;
        }
    }

    private int operationFromCell(int rowIndex) {
        return parseOperation(tableModel.getValueAt(rowIndex, 0));
    }

    private int parseOperation(Object value) {
        if (value == null) {
            return SpecialSpecService.OPERATION_ADD;
        }
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty() || raw.startsWith("1")) {
            return SpecialSpecService.OPERATION_ADD;
        }
        if (raw.startsWith("2")) {
            return SpecialSpecService.OPERATION_CHANGE;
        }
        if (raw.startsWith("3")) {
            return SpecialSpecService.OPERATION_DELETE;
        }
        throw new IllegalArgumentException("処理は1.追加/2.変更/3.削除のいずれかを選択してください。");
    }

    private String operationLabel(Integer operation) {
        if (operation == null || operation == SpecialSpecService.OPERATION_ADD) {
            return "1.追加";
        }
        if (operation == SpecialSpecService.OPERATION_CHANGE) {
            return "2.変更";
        }
        if (operation == SpecialSpecService.OPERATION_DELETE) {
            return "3.削除";
        }
        return "1.追加";
    }

    private record DeletedRow(int rowIndex, Object[] rowData) {
    }
}
