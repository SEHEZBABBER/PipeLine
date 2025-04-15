import 'bootstrap/dist/css/bootstrap.min.css';
import axios from 'axios';
import { useState } from 'react';
import { useNavigate } from "react-router-dom";


function Import() {
  const naviagte = useNavigate();
  const [formData, setFormData] = useState({
    host_no: '',
    port_no: '',
    database_name: '',
    user_name: '',
    password: '',
  });
  const [scuessMsg,setSucessMsg] = useState("");
  const [failureMsg,setFailureMsg] = useState("");

  function handleChange(e) {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  }

  function handleSubmit(e) {
    const form = e.target;
    if (!form.checkValidity()) {
      e.preventDefault();
      e.stopPropagation();
    } else {
      e.preventDefault(); // optional: prevent form from refreshing the page
      axios.post('http://localhost:8080/Importdata',formData,{withCredentials:true}).then((res)=>{
        // is the login was successul tnow we are moving to the pages that can be used only bt the authorised user only so i need to add teh kwt token to the cookies now
        if(res.data.Message){
          setSucessMsg(res.data.Message);
          setFailureMsg("");
        }
        console.log(res);
        naviagte('/selectTabels');
      }).catch((err)=>{
        console.log(err);
        if(err.response.data.Message){
          setFailureMsg(err.response.data.Message);
          setSucessMsg("");
        }
      })
      console.log('Form submitted with data:', formData);
      // You can now send `formData` to your backend
    }

    form.classList.add('was-validated');
  }

  return (
    <div className="d-flex flex-column justify-content-center align-items-center text-center" style={{ width: "100vw", height: "100vh", backgroundColor: "#FFFDD0" }}>
      <h1>Enter Data for verification</h1>

      <form
        className='mt-5 d-flex justify-content-center align-items-center text-center flex-column needs-validation'
        style={{ width: "40vw", height: "60vh" }}
        onSubmit={handleSubmit}
        noValidate
      >
        <input
          type="text"
          name="host_no"
          placeholder='Enter Host Number of the database here'
          className='form-control w-100 p-2 mt-3'
          value={formData.host_no}
          onChange={handleChange}
          required
        />
        <div className="invalid-feedback">Please enter a host number.</div>

        <input
          type="text"
          name="port_no"
          placeholder='Enter Server Port of the database here'
          className='form-control w-100 p-2 mt-3'
          value={formData.port_no}
          onChange={handleChange}
          required
        />
        <div className="invalid-feedback">Please enter a port number.</div>

        <input
          type="text"
          name="database_name"
          placeholder='Enter name of the database here'
          className='form-control w-100 p-2 mt-3'
          value={formData.database_name}
          onChange={handleChange}
          required
        />
        <div className="invalid-feedback">Please enter a database name.</div>

        <input
          type="text"
          name="user_name"
          placeholder='Enter name of the user here'
          className='form-control w-100 p-2 mt-3'
          value={formData.user_name}
          onChange={handleChange}
          required
        />
        <div className="invalid-feedback">Please enter a user name.</div>

        <input
          type="password"
          name="password"
          placeholder='Enter password here'
          className='form-control w-100 p-2 mt-3'
          value={formData.password}
          onChange={handleChange}
          required
        />
        <div className="invalid-feedback">Please enter a JWT token.</div>

        <button type="submit" className='w-100 btn btn-primary mt-5'>Submit</button>
        {scuessMsg ? (<p style={{color:"green"}}>{scuessMsg}</p>):<p></p>}
        {failureMsg ? (<p style={{color:"red"}}>{failureMsg}</p>):<p></p>}
      </form>
    </div>
  );
}

export default Import;



// fd14838827b51d518073b8ef32f2c44fbb66109e12e99de8df269e1476e21cd1