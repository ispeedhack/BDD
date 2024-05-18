import React from 'react';
import { Link } from 'react-router-dom';

const Footer = () => {
    return (
        <footer role='content-info'>
            <div className="footer-wrapper">
                <div className="notice">
                    <p>Welcome to our social networking site! Connect with friends, share updates, and explore new connections.</p>
                </div>
                <ul className="quick-links">
                    <li>
                        <Link to="/faq">FAQs</Link> {/* Use Link component for FAQ */}
                    </li>
                    <li>
                        <Link to="/privacy">Privacy Policy</Link> {/* Use Link component for Privacy Policy */}
                    </li>
                    <li>
                        <Link to="/terms">Terms and Conditions</Link> {/* Use Link component for Terms and Conditions */}
                    </li>
                </ul>
            </div>
            <div className="outro">
                <p>&copy; {new Date().getFullYear()} All rights reserved.</p>
            </div>
        </footer>
    );
};

export default Footer;
