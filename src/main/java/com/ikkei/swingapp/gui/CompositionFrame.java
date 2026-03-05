package com.ikkei.swingapp.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.springframework.stereotype.Component;

import com.ikkei.swingapp.domain.CompositionBean;
import com.ikkei.swingapp.service.CompositionService;
import java.util.HashSet;
import java.util.Set;

@Component
public class CompositionFrame extends JFrame {

    private static final String[] COLUMNS = {"親品番", "子品番", "レベル"};

    private final CompositionService compositionService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final Deque<DeletedRow> deletedRows = new ArrayDeque<>();

    public CompositionFrame(CompositionService compositionService) {
        super("構成画面");
        this.compositionService = compositionService;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 420);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(COLUMNS, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addButton = new JButton("追加");
        addButton.addActionListener(e -> tableModel.addRow(new Object[] {"", "", 1}));

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
        List<CompositionBean> rows = compositionService.findAll();
        for (CompositionBean row : rows) {
            tableModel.addRow(new Object[] {row.getParentPartNo(), row.getChildPartNo(), row.getLevel()});
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
                    tableModel.getValueAt(rowIndex, 2)
            };
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

    private void saveTable() {
        try {
            List<CompositionBean> rows = new ArrayList<>();
            CompositionBean row = new CompositionBean();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String parentPartNo = String.valueOf(tableModel.getValueAt(i, 0)).trim();
                String childPartNo = String.valueOf(tableModel.getValueAt(i, 1)).trim();
                String levelRaw = String.valueOf(tableModel.getValueAt(i, 2)).trim();

                if (parentPartNo.isEmpty() || childPartNo.isEmpty()) {
                    throw new IllegalArgumentException((i + 1) + "行目の親品番・子品番は必須です。");
                }

                int level = Integer.parseInt(levelRaw);
                row.setParentPartNo(parentPartNo);
                row.setChildPartNo(childPartNo);
                row.setLevel(level);
                
                rows.add(row);
            }

            compositionService.saveAll(rows);
            deletedRows.clear();
            JOptionPane.showMessageDialog(this, "構成テーブルを保存しました。", "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "レベルは数値で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "保存エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    private record DeletedRow(int rowIndex, Object[] rowData) {
    }
}
