import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";


function SelectTabels() {
  const [tables, setTables] = useState([]);
  const [selectedTables, setSelectedTables] = useState([]);
  const [failreMsg,setFailureMsg] = useState("");
  const navigate = useNavigate();
  useEffect(() => {
    axios
      .get("http://localhost:8080/getTables")
      .then((res) => {
        const str = res.data.Success;
        const data = str.split(",").filter((t) => t.trim() !== "");
        setTables(data);
      })
      .catch((err) => {
        setFailureMsg("could not fetch data")
        console.error("Error fetching tables:", err);
      });
  }, []);

  const handleCheckboxChange = (e) => {
    const { value, checked } = e.target;
    if (checked) {
      setSelectedTables((prev) => [...prev, value]);
    } else {
      setSelectedTables((prev) => prev.filter((table) => table !== value));
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log("Selected Tables:", selectedTables);
    // You can also send them to your backend here
    axios.post('http://localhost:8080/submit-tables',selectedTables).then((res)=>{
        navigate('/allcolumns');
        console.log(res);
    }).catch((err)=>console.log(err));
  };

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>{failreMsg?<span>{failreMsg}</span>:<span>Select Tables</span>}</h1>
      <form onSubmit={handleSubmit} style={styles.form}>
        <div style={styles.checkboxContainer}>
          {tables.map((table, idx) => (
            <label key={idx} style={styles.label}>
              <input
                type="checkbox"
                value={table}
                onChange={handleCheckboxChange}
              />
              {table}
            </label>
          ))}
        </div>
        <button type="submit" style={styles.button}>Submit</button>
      </form>
    </div>
  );
}

const styles = {
  container: {
    padding: "30px",
    fontFamily: "Arial, sans-serif",
    background: "#f9f9f9",
    borderRadius: "10px",
    maxWidth: "500px",
    margin: "50px auto",
    boxShadow: "0px 4px 10px rgba(0, 0, 0, 0.1)"
  },
  title: {
    textAlign: "center",
    color: "#333"
  },
  form: {
    display: "flex",
    flexDirection: "column",
    gap: "15px"
  },
  checkboxContainer: {
    display: "flex",
    flexDirection: "column",
    gap: "10px"
  },
  label: {
    fontSize: "16px",
    color: "#444"
  },
  button: {
    padding: "10px",
    fontSize: "16px",
    backgroundColor: "#007bff",
    color: "white",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer"
  }
};

export default SelectTabels;
