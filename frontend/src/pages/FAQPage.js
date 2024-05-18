import React from 'react';
import './FAQPage.css'; // Import the CSS file for styling

const FAQPage = () => {
    return (
        <div className="faq-page">
            <h1>Frequently Asked Questions</h1>
            <div className="faq-item">
                <h3>How do I create an account?</h3>
                <p>To create an account, click on the "Register" link at the top of the page and fill out the registration form with your details.</p>
            </div>
            <div className="faq-item">
                <h3>Can I change my username?</h3>
                <p>Yes, you can change your username in your account settings. Navigate to the profile settings and find the option to edit your username.</p>
            </div>
            <div className="faq-item">
                <h3>How do I add friends?</h3>
                <p>You can add friends by searching for their usernames or email addresses in the search bar and sending them a friend request.</p>
            </div>
            <div className="faq-item">
                <h3>How do I report inappropriate content?</h3>
                <p>If you come across inappropriate content, you can report it by clicking on the "Report" button next to the content. Our team will review the report and take appropriate action.</p>
            </div>
        </div>
    );
}

export default FAQPage;
