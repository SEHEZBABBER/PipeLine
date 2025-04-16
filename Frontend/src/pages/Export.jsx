import 'bootstrap/dist/css/bootstrap.min.css';
import { useState } from 'react';
import axios from 'axios';
function Export() {
  // const [successMsg,setSucessMsg] = useState("");
  const [formData, setFormData] = useState({
    file: null,
    delimiter: ',',
    host_no: '',
    port_no: '',
    database_name: '',
    user_name: '',
    password: '',
    target_table: '',
    create_table: false,
  });

  function handleChange(e) {
    const { name, value, type, checked, files } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : type === 'file' ? files[0] : value,
    });
  }

  async function handleSubmit(e) {
    const form = e.target;
    if (!form.checkValidity()) {
      e.preventDefault();
      e.stopPropagation();
      form.classList.add('was-validated');
      return;
    }
  
    e.preventDefault();
  
    try {
      if (!formData.file) {
        alert("Please select a file.");
        return;
      }
  
      // Upload to Cloudinary
      const cloudData = new FormData();
      cloudData.append("file", formData.file);
      cloudData.append("upload_preset", "upload_present"); // corrected preset name
  
      const cloudRes = await axios.post(
        "https://api.cloudinary.com/v1_1/dbusctnyq/auto/upload",
        cloudData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );
  
      const uploadedUrl = cloudRes.data.secure_url;
      console.log("Uploaded to Cloudinary:", uploadedUrl);
  
      // Send everything to your backend
      const backendPayload = {
        ...formData,
        file: uploadedUrl,
      };

      console.log(backendPayload);
      const res = await axios.post("http://localhost:8080/upload", backendPayload);
      console.log("Backend response:", res.data);
    } catch (error) {
      console.error("Something went wrong:", error.response?.data || error.message);
      alert("Upload failed. Please check console for details.");
    }
  
    form.classList.add('was-validated');
  }
  return (
    <div
      className="d-flex flex-column justify-content-center align-items-center text-center"
      style={{ width: '100vw', height: '100vh', backgroundColor: '#FFFDD0' }}
    >
      <h1 className='mb-5'>Enter Data for Export to ClickHouse</h1>

      <form
        className="mt-5 d-flex justify-content-center align-items-center text-center flex-column needs-validation"
        style={{ width: '40vw', height: '60vh' }}
        onSubmit={handleSubmit}
        noValidate
      >
        <input
          type="file"
          name="file"
          accept=".csv,.txt"
          className="form-control w-100 p-2 mt-3"
          onChange={handleChange}
          required
        />
        <div className="invalid-feedback">Please upload a CSV or text file.</div>

        <select
          name="delimiter"
          className="form-control w-100 p-2 mt-3"
          value={formData.delimiter}
          onChange={handleChange}
          required
        >
          <option value=",">Comma (,)</option>
          <option value=";">Semicolon (;)</option>
          <option value="\t">Tab</option>
          <option value="|">Pipe (|)</option>
        </select>
        <div className="invalid-feedback">Please select a delimiter.</div>

        <input
          type="text"
          name="host_no"
          placeholder="Enter Host Number of the database here"
          className="form-control w-100 p-2 mt-3"
          value={formData.host_no}
          onChange={handleChange}
          required
        />
        <div className="invalid-feedback">Please enter a host number.</div>

        <input
          type="text"
          name="port_no"
          placeholder="Enter Server Port of the database here"
          className="form-control w-100 p-2 mt-3"
          value={formData.port_no}
          onChange={handleChange}
          required
        />
        <div className="invalid-feedback">Please enter a port number.</div>

        <input
          type="text"
          name="database_name"
          placeholder="Enter name of the database here"
          className="form-control w-100 p-2 mt-3"
          value={formData.database_name}
          onChange={handleChange}
          required
        />
        <div className="invalid-feedback">Please enter a database name.</div>

        <input
          type="text"
          name="user_name"
          placeholder="Enter name of the user here"
          className="form-control w-100 p-2 mt-3"
          value={formData.user_name}
          onChange={handleChange}
          required
        />
        <div className="invalid-feedback">Please enter a user name.</div>

        <input
          type="password"
          name="password"
          placeholder="Enter JWT token here"
          className="form-control w-100 p-2 mt-3"
          value={formData.password}
          onChange={handleChange}
          required
        />
        <div className="invalid-feedback">Please enter a JWT token.</div>

        <input
          type="text"
          name="target_table"
          placeholder="Enter target table name here"
          className="form-control w-100 p-2 mt-3"
          value={formData.target_table}
          onChange={handleChange}
          required
        />
        <div className="invalid-feedback">Please enter a target table name.</div>

        <div className="form-check w-100 mt-3">
          <input
            type="checkbox"
            name="create_table"
            className="form-check-input"
            checked={formData.create_table}
            onChange={handleChange}
          />
          <label className="form-check-label">Create table if it doesn’t exist</label>
        </div>

        <button type="submit" className="w-100 btn btn-primary mt-5">
          Submit
        </button>
      </form>
    </div>
  );
}

export default Export;