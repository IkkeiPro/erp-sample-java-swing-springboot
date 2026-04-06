package com.ikkei.swingapp.gui.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.ikkei.swingapp.domain.ProductMasterViewBean;

public class ProductMasterTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"商品コード", "商品名", "更新担当者コード", "更新担当者名"};

    private final List<ProductMasterViewBean> rows = new ArrayList<>();

    public void setRows(List<ProductMasterViewBean> newRows) {
        rows.clear();
        rows.addAll(newRows);
        fireTableDataChanged();
    }

    public void addEmptyRow() {
        rows.add(new ProductMasterViewBean("", "", "", ""));
        int index = rows.size() - 1;
        fireTableRowsInserted(index, index);
    }

    public void removeRow(int rowIndex) {
        rows.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public List<ProductMasterViewBean> getRows() {
        return rows;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ProductMasterViewBean row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getProductCode();
            case 1 -> row.getProductName();
            case 2 -> row.getUpdaterCode();
            case 3 -> row.getUpdaterName();
            default -> "";
        };
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        ProductMasterViewBean row = rows.get(rowIndex);
        String text = value == null ? "" : String.valueOf(value);

        switch (columnIndex) {
            case 0 -> row.setProductCode(text);
            case 1 -> row.setProductName(text);
            case 2 -> row.setUpdaterCode(text);
            default -> {
                return;
            }
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 3;
    }
}
