import { useContext } from "react";
import { UserContext } from "../userContext";
import { Link } from "react-router-dom";

function Header(props) {
    return (
        
        <header className="bg-dark text-light py-3">
            <div className="container d-flex justify-content-between align-items-center">
                <h1>{props.title}</h1>
                <nav>
                    <ul className="list-inline mb-0">
                        <li className="list-inline-item">
                            <Link to="/" className="nav-link">Home</Link>
                        </li>
                        <UserContext.Consumer>
                        {context =>
                        context.user ? (
                            <>
                                <li className="list-inline-item">
                                    <Link to="/logout" className="nav-link">Logout</Link>
                                </li>
                            </>
                            ) : (
                            <>
                                <li className="list-inline-item">
                                    <Link to="/login" className="nav-link">Login</Link>
                                </li>
                                <li className="list-inline-item">
                                    <Link to="/register" className="nav-link">Register</Link>
                                </li>
                            </>
                            )
                        }
                        </UserContext.Consumer>
                    </ul>
                </nav>
            </div>
        </header>
    );
}

export default Header;