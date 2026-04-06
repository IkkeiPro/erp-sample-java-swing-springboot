package com.ikkei.swingapp.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.springframework.stereotype.Component;

import com.ikkei.swingapp.domain.ProductMasterViewBean;
import com.ikkei.swingapp.gui.model.ProductMasterTableModel;
import com.ikkei.swingapp.service.ProductMasterService;

@Component
public class ProductMasterFrame extends JFrame {

    private final ProductMasterService productMasterService;
    private final ProductMasterTableModel tableModel;
    private final JTable table;

    private List<ProductMasterViewBean> originalRows = new ArrayList<>();

    public ProductMasterFrame(ProductMasterService productMasterService) {
        super("商品マスタ一覧・更新画面");
        this.productMasterService = productMasterService;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(880, 420);
        setLocationRelativeTo(null);

        tableModel = new ProductMasterTableModel();
        table = new JTable(tableModel);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addButton = new JButton("追加");
        addButton.addActionListener(e -> tableModel.addEmptyRow());

        JButton deleteButton = new JButton("削除");
        deleteButton.addActionListener(e -> deleteSelectedRows());

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> saveTable());

        JButton closeButton = new JButton("閉じる");
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        reloadTable();
    }

    public void reloadTable() {
        List<ProductMasterViewBean> rows = productMasterService.findAllForView();
        originalRows = deepCopy(rows);
        tableModel.setRows(deepCopy(rows));
    }

    private void saveTable() {
        try {
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }

            productMasterService.saveFromViewBeans(tableModel.getRows(), originalRows);
            reloadTable();
            JOptionPane.showMessageDialog(this, "商品マスタを保存しました。", "成功", JOptionPane.INFORMATION_MESSAGE);
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
            tableModel.removeRow(selected[i]);
        }
    }

    private List<ProductMasterViewBean> deepCopy(List<ProductMasterViewBean> rows) {
        List<ProductMasterViewBean> copied = new ArrayList<>();
        for (ProductMasterViewBean row : rows) {
            copied.add(new ProductMasterViewBean(
                    row.getProductCode(),
                    row.getProductName(),
                    row.getUpdaterCode(),
                    row.getUpdaterName()));
        }
        return copied;
    }
}
