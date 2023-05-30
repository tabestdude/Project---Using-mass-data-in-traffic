import { useContext, useState } from 'react';
import { UserContext } from '../userContext';
import { Navigate } from 'react-router-dom';

function Login(){
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const userContext = useContext(UserContext); 

    async function Login(e){
        console.log("Login");
        e.preventDefault();
        const res = await fetch("http://localhost:3080/users/login", {
            method: "POST",
            credentials: "include",
            headers: { 'Content-Type': 'application/json'},
            body: JSON.stringify({
                username: username,
                password: password
            })
        });
        /*const res = await fetch("http://localhost:3001/users/login", {
            method: "POST",
            credentials: "include",
            headers: { 'Content-Type': 'application/json'},
            body: JSON.stringify({
                username: username,
                password: password
            })
        });*/
        const data = await res.json();
        if(data._id !== undefined){
            userContext.setUserContext(data);
        } else {
            setUsername("");
            setPassword("");
            setError("Invalid username or password");
        }
    }

    return (
        <form onSubmit={Login} className="container mt-5">
            {userContext.user ? <Navigate replace to="/" /> : ""}
            <div className="form-group">
                <label htmlFor="usernameInput">Username</label>
                <input
                type="text"
                className="form-control"
                id="usernameInput"
                placeholder="Username"
                name="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                />
            </div>
            <div className="form-group">
                <label htmlFor="passwordInput">Password</label>
                <input
                type="password"
                className="form-control"
                id="passwordInput"
                placeholder="Password"
                name="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                />
            </div>
            <button type="submit" className="btn btn-primary">Log in</button>
            {error && <label className="text-danger">{error}</label>}
        </form>
        
    );
}

export default Login;