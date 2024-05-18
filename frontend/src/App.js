import React from 'react';
import {
  Route,
  BrowserRouter as Router,
  Routes
} from "react-router-dom";

import './App.css';
import Footer from './components/Footer';
import Navbar from './components/Navbar';
import AboutPage from './pages/AboutPage';
import ContactPage from './pages/ContactPage';
import FAQPage from './pages/FAQPage'; // Import FAQPage component
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import PrivacyPage from './pages/PrivacyPage';
import RegisterPage from './pages/RegisterPage';
import TermsPage from './pages/TermsPage';

function App() {
  return (
    <Router>
      <div className="App">
        <Navbar />
        <Routes>
          <Route exact path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/contact" element={<ContactPage />} />
          <Route path="/terms" element={<TermsPage />} />
          <Route path="/privacy" element={<PrivacyPage />} />
          <Route path="/about" element={<AboutPage />} />
          <Route path="/faq" element={<FAQPage />} /> {/* Add route for FAQPage */}
        </Routes>
        <div style={{ height: 200 }}></div>
        <Footer />
      </div>
    </Router>
  );
}

export default App;
