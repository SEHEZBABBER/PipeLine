🔄 Bidirectional ClickHouse & Flat File Data Ingestion Tool
A web-based application that enables seamless bidirectional data transfer between ClickHouse databases and flat files, implemented using JDBC for database connectivity.
✨ Overview
This application provides a user-friendly interface to transfer data:

📊 ClickHouse → Flat File: Export data from your database to structured flat files
📁 Flat File → ClickHouse: Import data from flat files into your database

The tool allows users to select specific columns for ingestion and reports the total number of records processed upon completion.
🚀 Features

↔️ Bidirectional Data Flow: Transfer data in both directions between ClickHouse and flat files
🔍 Source Selection: Choose between ClickHouse or flat file as your data source
✅ Column Selection: Select specific columns to include in the data transfer
🔐 JWT Authentication: Connect to ClickHouse securely using JWT token-based authentication
🔎 Schema Discovery: Automatically detect and display available tables and columns
📊 Completion Reporting: View the total count of processed records upon successful ingestion
⚠️ Error Handling: User-friendly error messages for connection, authentication, and data transfer issues

💻 Technology Stack

Backend: Java with JDBC for database connectivity , Spring Boot
Frontend: HTML, CSS, JavaScript , React Js
Database: ClickHouse
File Handling: Java I/O libraries for flat file operations

📋 Prerequisites

☕ Java Development Kit (JDK) 11 or higher
🛠️ Apache Maven (for build management)
🌐 Web browser
🗄️ ClickHouse instance (local Docker or cloud-based)

🔧 Installation and Setup

Clone the repository:
bashgit clone https://github.com/SEHEZBABBER/PipeLine
cd clickhouse-flat-file-ingestion

Build the project:
bashmvn clean install

Run the application:
Run BackendApplication.java file 

Backend Port : 8080

Access the web interface:
http://localhost:5173


📝 Usage Guide
📊 ClickHouse to Flat File Transfer

Select Either to import Data or Export Data:

Provide Necessary Credentials
🖥️ Host (e.g., localhost)
🔌 Port (e.g., 9440 for HTTPS, 9000 for HTTP)
🗃️ Database name
👤 Username
🔑 password

Select tables and columns then you will be able to see file preview and downlaod it

For Exporting Data 
Select the source File data in the file should start from the 2nd line 
choose delimiter according to your file
Provide Necessary Credentials
🖥️ Host (e.g., localhost)
🔌 Port (e.g., 9440 for HTTPS, 9000 for HTTP)
🗃️ Database name
👤 Username
🔑 password

Data will be added then

⚙️ Data Processing

Efficient batch processing of records during data transfer
Type mapping between ClickHouse data types and flat file representations
Transaction management for reliable data ingestion

🧪 Testing
The application has been tested with:

ClickHouse example datasets (uk_price_paid and ontime)
Various flat file formats with different delimiters
Edge cases for error handling

❓ Troubleshooting
Common issues and solutions:

🔴 Connection Failure:

Verify ClickHouse host and port
Check if JWT token is valid and not expired
Ensure ClickHouse server is running


📁 File Access Error:

Verify file path and permissions
Check if file format matches the configured delimiter


⚠️ Data Type Mismatch:

Ensure compatibility between ClickHouse columns and flat file data types


🌟 Quick Start
For the fastest setup experience:
bash# Clone repository
git clone https://github.com/yourusername/clickhouse-flat-file-ingestion.git
