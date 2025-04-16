package com.example.Backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173",allowCredentials = "true")
public class userRoutes {
    private static Object castToType(String value, String type) {
        if (value == null || value.trim().isEmpty()) return null;

        try {
            switch (type.toLowerCase()) {
                case "int":
                case "int32":
                case "integer":
                    return Integer.parseInt(value.trim());
                case "int64":
                case "bigint":
                    return Long.parseLong(value.trim());
                case "float":
                case "float32":
                    return Float.parseFloat(value.trim());
                case "float64":
                case "double":
                    return Double.parseDouble(value.trim());
                case "boolean":
                case "bool":
                    return Boolean.parseBoolean(value.trim());
                case "date":
                case "datetime":
                    return "'" + value.trim() + "'"; // ClickHouse expects date/time in quotes
                default:
                    return value.trim(); // Treat as String
            }
        } catch (Exception e) {
            return null; // If parsing fails, return NULL
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestBody Map<String, String> uploaddata) {
        System.out.println(uploaddata);

        Response_conn_DB.setHostNo(uploaddata.get("host_no"));
        Response_conn_DB.setUserName(uploaddata.get("user_name"));
        Response_conn_DB.setServerNo(uploaddata.get("port_no"));
        Response_conn_DB.setDatabaseName(uploaddata.get("database_name"));
        Response_conn_DB.setPassword(uploaddata.get("password"));

        String fileUrl = uploaddata.get("file");
        String delimiter = uploaddata.get("delimiter");
        String targetTable = uploaddata.get("target_table");
        String createNew = uploaddata.get("create_table");

        try (Connection conn = ConnectionManager.getConn()) {
            // Step 1: Read CSV
            InputStream inputStream = new URL(fileUrl).openStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(new CSVParserBuilder().withSeparator(delimiter.charAt(0)).build())
                    .build();

            List<String[]> allRows = csvReader.readAll();
            if (allRows.isEmpty()) throw new RuntimeException("Empty CSV file.");

            String[] headers = allRows.get(1); // Correct header row
            List<String[]> dataRows = allRows.subList(2, allRows.size());

            Statement stmt = conn.createStatement();

            // Step 2: Create table if required
            if ("true".equalsIgnoreCase(createNew)) {
                StringBuilder createSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " + targetTable + " (");
                for (int i = 0; i < headers.length; i++) {
                    createSQL.append(headers[i]).append(" String"); // Default to String
                    if (i < headers.length - 1) createSQL.append(", ");
                }
                createSQL.append(") ENGINE = MergeTree() ORDER BY tuple();");
                stmt.execute(createSQL.toString());
            }

            // Step 3: Read actual schema
            Map<String, String> columnTypes = new LinkedHashMap<>(); // ordered
            ResultSet rs = stmt.executeQuery("DESCRIBE TABLE " + targetTable);
            while (rs.next()) {
                columnTypes.put(rs.getString("name"), rs.getString("type"));
                System.out.println(columnTypes);
            }

            // Step 4: Insert data row by row
            List<String> valueGroups = new ArrayList<>();
            for (String[] row : dataRows) {
                Map<String, String> csvData = new LinkedHashMap<>();
                for (int i = 0; i < headers.length && i < row.length; i++) {
                    csvData.put(headers[i], row[i]);
                }

                List<String> formattedValues = new ArrayList<>();
                for (String col : columnTypes.keySet()) {
                    String value = csvData.getOrDefault(col, null);
                    String type = columnTypes.get(col);

                    if (value == null || value.isBlank()) {
                        formattedValues.add("NULL");
                    } else {
                        formattedValues.add(formatValueForClickHouse(value, type));
                    }
                }

                valueGroups.add("(" + String.join(",", formattedValues) + ")");
            }

            // Step 5: Final insert
            String insertSQL = "INSERT INTO " + targetTable + " VALUES " + String.join(",", valueGroups) + ";";
            stmt.execute(insertSQL);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> failureMsg = new HashMap<>();
            failureMsg.put("Message", "Upload failed: " + e.getMessage());
            return new ResponseEntity<>(failureMsg, HttpStatus.BAD_REQUEST);
        }

        Map<String, String> successMsg = new HashMap<>();
        successMsg.put("Message", "Upload successful.");
        return new ResponseEntity<>(successMsg, HttpStatus.OK);
    }

    private String formatValueForClickHouse(String value, String type) {
        try {
            value = value.trim();
            if (type.startsWith("Nullable(")) {
                type = type.substring(9, type.length() - 1);
            }

            return switch (type) {
                case "Int8", "Int16", "Int32", "Int64",
                     "UInt8", "UInt16", "UInt32", "UInt64" -> String.valueOf(Long.parseLong(value));
                case "Float32", "Float64" -> String.valueOf(Double.parseDouble(value));
                case "Date", "Date32", "DateTime", "DateTime64" ->
                        "'" + value.replace("'", "''") + "'";
                case "String", "UUID" ->
                        "'" + value.replace("'", "''") + "'";
                default -> "'" + value.replace("'", "''") + "'";
            };
        } catch (Exception e) {
            return "NULL";
        }
    }


    @GetMapping("/get-generated-file")
    public static ResponseEntity<?> getGeneratedFile() {
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
