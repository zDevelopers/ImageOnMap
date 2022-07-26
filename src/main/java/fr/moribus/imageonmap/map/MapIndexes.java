package fr.moribus.imageonmap.map;

public class MapIndexes {

    private final int columnIndex;
    private final int rowIndex;

    public MapIndexes(int rowIndex, int columnIndex) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }
}
