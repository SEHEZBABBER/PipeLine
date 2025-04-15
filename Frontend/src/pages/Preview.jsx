import { useEffect, useState } from "react";
import axios from "axios";
import Papa from "papaparse";

function Preview() {
  const [csvData, setCsvData] = useState([]);
  const [csvFileUrl, setCsvFileUrl] = useState("");

  useEffect(() => {
    axios.get("http://localhost:8080/get-generated-file")
      .then((res) => {
        const blob = new Blob([res.data], { type: "text/csv" });
        const fileUrl = URL.createObjectURL(blob);
        setCsvFileUrl(fileUrl);

        Papa.parse(res.data, {
          header: true,
          skipEmptyLines: true,
          complete: function (results) {
            setCsvData(results.data);
          }
        });
      })
      .catch((err) => {
        console.error("Error fetching file:", err);
      });
  }, []);

  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
      <h1>CSV Preview</h1>
      {csvData.length > 0 ? (
        <table border="1" cellPadding="8">
          <thead>
            <tr>
              {Object.keys(csvData[0]).map((col, idx) => (
                <th key={idx}>{col}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {csvData.map((row, rIdx) => (
              <tr key={rIdx}>
                {Object.values(row).map((val, cIdx) => (
                  <td key={cIdx}>{val}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p>Loading CSV data...</p>
      )}

      {csvFileUrl && (
        <a
          href={csvFileUrl}
          download="exported-data.csv"
          style={{
            marginTop: "20px",
            padding: "10px 20px",
            backgroundColor: "#4CAF50",
            color: "white",
            textDecoration: "none",
            borderRadius: "5px"
          }}
        >
          Download CSV
        </a>
      )}
    </div>
  );
}

export default Preview;
