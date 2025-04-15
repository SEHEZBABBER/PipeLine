package com.example.Backend;

import java.util.ArrayList;
import java.util.List;

public class SelectedTables {
    private static final List<String> selectedTables = new ArrayList<>();

    // Getter method to return the tables as an array
    public static String[] getTables() {
        return selectedTables.toArray(new String[0]);
    }

    // Add a table name (if not already added)
    public static void add(String tableName) {
        if (!selectedTables.contains(tableName)) {
            selectedTables.add(tableName);
        }
    }

    // Optional: Clear selected tables (if needed somewhere)
    public static void clear() {
        selectedTables.clear();
    }
}
