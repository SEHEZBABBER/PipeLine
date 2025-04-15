package com.example.Backend;

import com.google.common.collect.Table;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173",allowCredentials = "true")
public class userRoutes {
    @GetMapping("/get-generated-file")
    public ResponseEntity<?> getGeneratedFile() {
        try {
            String[] tables = SelectedTables.getTables();
            Map<String, List<String>> selectedColumnsMap = SelectedColumns.getColumnsMap();

            if (tables == null || selectedColumnsMap == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No tables or columns selected.");
            }

            Connection conn = ConnectionManager.getConn();
            String useDbQuery = "USE " + Response_conn_DB.getDatabaseName();
            conn.prepareStatement(useDbQuery).execute();

            StringBuilder csvBuilder = new StringBuilder();

            for (String table : tables) {
                if (!selectedColumnsMap.containsKey(table)) continue;

                List<String> columns = selectedColumnsMap.get(table);
                if (columns == null || columns.isEmpty()) continue;

                // Write CSV headers
                csvBuilder.append("Table: ").append(table).append("\n");
                csvBuilder.append(String.join(",", columns)).append("\n");

                // Build SQL query
                String columnQuery = String.join(", ", columns);
                String sql = "SELECT " + columnQuery + " FROM `" + table + "`";

                ResultSet rs = conn.prepareStatement(sql).executeQuery();
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    for (String col : columns) {
                        row.add(rs.getString(col));
                    }
                    csvBuilder.append(String.join(",", row)).append("\n");
                }

                csvBuilder.append("\n"); // Blank line between tables
            }

            // Return CSV content as plain text
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=generated.csv")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(csvBuilder.toString());

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SQL Error: " + e.getMessage());
        }
    }

    @PostMapping("/save-columns")
    public static ResponseEntity<?> saveSelectedColumns(@RequestBody Map<String, List<String>> columnsFromFrontend) {
        try {
            String[] selectedTables = SelectedTables.getTables();
            if (selectedTables == null || selectedTables.length == 0) {
                return ResponseEntity.badRequest().body(Map.of("status", "failure", "message", "No tables were selected."));
            }

            // Use database
            Connection conn = ConnectionManager.getConn();
            PreparedStatement useDb = conn.prepareStatement("USE " + Response_conn_DB.getDatabaseName());
            useDb.execute();

            // Verify columns for each table
            for (Map.Entry<String, List<String>> entry : columnsFromFrontend.entrySet()) {
                String tableName = entry.getKey();
                List<String> selectedColumns = entry.getValue();

                if (!Arrays.asList(selectedTables).contains(tableName)) {
                    return ResponseEntity.badRequest().body(Map.of("status", "failure", "message", "Invalid table: " + tableName));
                }

                // Get actual columns from DB
                PreparedStatement columnStmt = conn.prepareStatement("SHOW COLUMNS FROM `" + tableName + "`");
                ResultSet rs = columnStmt.executeQuery();

                List<String> dbColumns = new ArrayList<>();
                while (rs.next()) {
                    dbColumns.add(rs.getString(1));
                }

                // Validate selected columns exist in actual columns
                for (String col : selectedColumns) {
                    if (!dbColumns.contains(col)) {
                        return ResponseEntity.badRequest().body(Map.of("status", "failure", "message", "Column '" + col + "' does not exist in table '" + tableName + "'"));
                    }
                }

                // Save validated columns
                SelectedColumns.setColumns(tableName, selectedColumns);
            }

            return ResponseEntity.ok(Map.of("status", "success", "message", "Selected columns saved successfully."));

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "SQL error: " + e.getMessage()));
        }
    }
    @GetMapping("/get-columns")
    public static ResponseEntity<?> getColumns() {
        try {
            // Step 1: Get selected tables
            String[] tablesFromFrontend = SelectedTables.getTables();
            if (tablesFromFrontend == null || tablesFromFrontend.length == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("status", "failure", "message", "No tables have been selected."));
            }

            // Step 2: Use selected database
            Connection conn = ConnectionManager.getConn();
            String useDbQuery = "USE " + Response_conn_DB.getDatabaseName();
            PreparedStatement useDatabase = conn.prepareStatement(useDbQuery);
            useDatabase.execute();

            // Step 3: Retrieve columns
            Map<String, List<String>> tableColumnsMap = new HashMap<>();

            for (String table : tablesFromFrontend) {
                PreparedStatement columnStmt = conn.prepareStatement("SHOW COLUMNS FROM `" + table + "`");
                ResultSet columnRs = columnStmt.executeQuery();
                List<String> columns = new ArrayList<>();

                while (columnRs.next()) {
                    columns.add(columnRs.getString(1)); // 1st column = column name
                }

                tableColumnsMap.put(table, columns);
            }

            // Step 4: Return columns
            return ResponseEntity.ok(Map.of("status", "success", "columns", tableColumnsMap));

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "SQL error: " + e.getMessage()));
        }
    }
    @PostMapping("/submit-tables")
    public static ResponseEntity<?> saveTables(@RequestBody String[] tablesFromFrontend) {
        for(String table_name : tablesFromFrontend)SelectedTables.add(table_name);
        try {
            Connection conn = ConnectionManager.getConn();

            // Select the desired database
            String useDbQuery = "USE " + Response_conn_DB.getDatabaseName();
            PreparedStatement useDatabase = conn.prepareStatement(useDbQuery);
            useDatabase.execute();

            // Step 1: Fetch all table names from DB
            PreparedStatement showTables = conn.prepareStatement("SHOW TABLES");
            ResultSet rs = showTables.executeQuery();

            Set<String> existingTables = new HashSet<>();
            while (rs.next()) {
                existingTables.add(rs.getString(1).toLowerCase()); // Make case-insensitive comparison
            }

            // Step 2: Check if all tables from frontend exist
            List<String> notFound = new ArrayList<>();
            for (String table : tablesFromFrontend) {
                if (!existingTables.contains(table.toLowerCase())) {
                    notFound.add(table);
                }
            }


            if (!notFound.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "failure");
                errorResponse.put("message", "The following tables were not found in the database");
                errorResponse.put("missingTables", notFound);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            // Step 4: Return columns as success message
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("status", "success");

            return ResponseEntity.ok(successResponse);

        } catch (SQLException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "SQL error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/getTables")
    public static ResponseEntity<?> giveTables() {
        try {
            Connection conn = ConnectionManager.getConn();

            // Directly using the database name in the query
            String useDbQuery = "USE " + Response_conn_DB.getDatabaseName();
            PreparedStatement useDatabase = conn.prepareStatement(useDbQuery);
            useDatabase.execute();

            PreparedStatement storeTables = conn.prepareStatement("SHOW TABLES");
            ResultSet rs = storeTables.executeQuery();

            StringBuilder tables = new StringBuilder();
            while (rs.next()) {
                tables.append(rs.getString(1)).append(",");
            }

            // Remove trailing comma if exists
            if (tables.length() > 0) {
                tables.setLength(tables.length() - 1);
            }

            Map<String, String> successMsg = new HashMap<>();
            successMsg.put("Success", tables.toString());

            return ResponseEntity.status(HttpStatus.OK).body(successMsg);

        } catch (SQLException e) {
            Map<String, String> failureMsg = new HashMap<>();
            failureMsg.put("Error", "Failed to load tables: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failureMsg);
        }
    }

    @PostMapping("/Importdata")
    public static ResponseEntity<Map<String,String>> Import(@RequestBody Map<String,String> ImportData){
        // host no server no username jwt token data base name
        System.out.println(ImportData);
        if(ImportData.get("host_no") == null){
            Map<String,String> errorResponse = new HashMap<>();
            errorResponse.put("Message","Missing Required Field : hostNo ");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        if(ImportData.get("port_no") == null){
            Map<String,String> errorResponse = new HashMap<>();
            errorResponse.put("Message","Missing Required Field : hostNo ");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        if(ImportData.get("user_name") == null){
            Map<String,String> errorResponse = new HashMap<>();
            errorResponse.put("Message","Missing Required Field : hostNo ");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        if(ImportData.get("password") == null){
            Map<String,String> errorResponse = new HashMap<>();
            errorResponse.put("Message","Missing Required Field : hostNo ");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        if(ImportData.get("database_name") == null){
            Map<String,String> errorResponse = new HashMap<>();
            errorResponse.put("Message","Missing Required Field : hostNo ");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        else{
            // we are Done with Backend Validation
            Response_conn_DB.setHostNo(ImportData.get("host_no"));
            Response_conn_DB.setDatabaseName(ImportData.get("database_name"));
            Response_conn_DB.setPassword(ImportData.get("password"));
            Response_conn_DB.setServerNo(ImportData.get("port_no"));
            Response_conn_DB.setUserName(ImportData.get("user_name"));
            Map<String,String> successMessage= new HashMap<String,String>();
            try{
                Connection conn = ConnectionManager.getConn();
                successMessage.put("Message","Connected to DataBase Successfully");
                return new ResponseEntity<>(successMessage,HttpStatus.OK);
            } catch (SQLException e) {
                Map<String,String> failureMessage = new HashMap<String,String>();
                failureMessage.put("Message","Connection Failed");
                return new ResponseEntity<>(failureMessage,HttpStatus.BAD_REQUEST);
            }
        }
    }
}
