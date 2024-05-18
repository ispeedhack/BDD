import React from 'react';
import { Link } from 'react-router-dom';

function Navbar() {
    return (
        <nav className='navbar'>
            <div className="nav-wrapper">
                <div className="logo-box">logo</div>
                <ul className='nav-links'>
                    <li>
                        <Link to="/">Home</Link>
                    </li>
                    <li>
                        <Link to="/about">About</Link>
                    </li>
                    <li>
                        <Link to="/contact">Contact</Link>
                    </li>
                </ul>
                <div className="auth-btns-box">
                    <Link to="/login" className="auth-btn login">Login</Link>
                    <Link to="/register" className="auth-btn register">Register</Link>
                </div>
            </div>
        </nav>
    );
}

export default Navbar;
