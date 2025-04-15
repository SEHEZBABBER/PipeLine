import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function Columns() {
    const [columnsData, setColumnsData] = useState({});
    const [selectedColumns, setSelectedColumns] = useState({});
    const navigate = useNavigate();
    useEffect(() => {
        axios.get("http://localhost:8080/get-columns")
            .then((res) => {
                if (res.data.status === "success") {
                    setColumnsData(res.data.columns);
                    // Initialize selectedColumns with empty arrays for each table
                    const initialState = {};
                    Object.keys(res.data.columns).forEach(table => {
                        initialState[table] = [];
                    });
                    setSelectedColumns(initialState);
                } else {
                    console.error("Failed to fetch columns");
                }
            })
            .catch((err) => {
                console.error("Error fetching columns:", err);
            });
    }, []);

    const handleCheckboxChange = (table, column) => {
        setSelectedColumns(prev => {
            const isAlreadySelected = prev[table]?.includes(column);
            const updated = isAlreadySelected
                ? prev[table].filter(col => col !== column)
                : [...prev[table], column];
            return { ...prev, [table]: updated };
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log("Submitted Columns: ", selectedColumns);

        axios.post("http://localhost:8080/save-columns", selectedColumns).then((res)=>{
            console.log(res);
            navigate('/preview')
        }).catch((err)=>{
            console.log(err);
        })

    };

    return (
        <div style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            minHeight: "100vh",
            backgroundColor: "#f4f4f4"
        }}>
            <form onSubmit={handleSubmit} style={{
                backgroundColor: "white",
                padding: "30px",
                borderRadius: "10px",
                boxShadow: "0 4px 8px rgba(0,0,0,0.1)",
                width: "400px"
            }}>
                <h2 style={{ textAlign: "center", marginBottom: "20px" }}>Select Columns</h2>

                {Object.entries(columnsData).map(([table, columns]) => (
                    <div key={table} style={{ marginBottom: "20px" }}>
                        <h4 style={{ marginBottom: "10px" }}>{table}</h4>
                        {columns.map(column => (
                            <div key={column} style={{ marginLeft: "10px" }}>
                                <label>
                                    <input
                                        type="checkbox"
                                        checked={selectedColumns[table]?.includes(column) || false}
                                        onChange={() => handleCheckboxChange(table, column)}
                                    />
                                    {" "}{column}
                                </label>
                            </div>
                        ))}
                    </div>
                ))}

                <button type="submit" style={{
                    display: "block",
                    margin: "0 auto",
                    padding: "10px 20px",
                    backgroundColor: "#007BFF",
                    color: "white",
                    border: "none",
                    borderRadius: "5px",
                    cursor: "pointer"
                }}>
                    Submit
                </button>
            </form>
        </div>
    );
}

export default Columns;
