package com.example.Backend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectedColumns {
    private static final Map<String, List<String>> selectedColumnsMap = new HashMap<>();

    public static void setColumns(String tableName, List<String> columns) {
        selectedColumnsMap.put(tableName, columns);
    }

    public static Map<String, List<String>> getColumnsMap() {
        return selectedColumnsMap;
    }
}
