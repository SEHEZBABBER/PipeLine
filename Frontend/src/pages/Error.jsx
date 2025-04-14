import { useNavigate } from "react-router-dom";
import './Error.css';

function Error() {
    let navigate = useNavigate();
    
    function handleClick() {
        navigate('/Home');
    }

    return ( 
        <div className="error-container">
            <h1 className="error-heading">Oops! You're not at a correct URL</h1>
            <p className="error-link" onClick={handleClick}>Go To Home Page</p>
        </div>
    );
}

export default Error;
