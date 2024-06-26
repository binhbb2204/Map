package util;
import java.util.*;

public class Matrix<T>{
    private List<List<T>> table;

    public void setDimension(int rows, int cols){
        table = new ArrayList<List<T>>(rows);
        for(int i = 0; i < rows; i++){
            ArrayList<T> row = new ArrayList<>(cols);
            for(int j = 0; j < cols; j++){
                row.add(null);
            }
            table.add(row);

        }
    }

    public int getRow(){
        return table != null ? table.size() : 0;
    }

    public int getColumn(){
        return getRow() > 0 ? table.get(0).size() : 0;
    }

    public T getValue(int row, int col){
        return table.get(row).get(col);
    }

    public T setValue(int row, int col, T value){
        return table.get(row).set(col, value);
    }

}
