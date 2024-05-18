import React from 'react';

const ContactPage = () => {
    return (
        <div className="contact-page">
            <h1>Contact Us</h1>
            <p>If you have any questions, suggestions, or feedback, please feel free to reach out to us. We'd love to hear from you!</p>
            <p>You can contact us via email at <a href="mailto:info@example.com">info@example.com</a> or through the form below:</p>
            <form>
                <div className="form-group">
                    <label htmlFor="name">Name:</label>
                    <input type="text" id="name" name="name" required />
                </div>
                <div className="form-group">
                    <label htmlFor="email">Email:</label>
                    <input type="email" id="email" name="email" required />
                </div>
                <div className="form-group">
                    <label htmlFor="message">Message:</label>
                    <textarea id="message" name="message" rows="4" required></textarea>
                </div>
                <button type="submit">Send Message</button>
            </form>
        </div>
    );
};

export default ContactPage;
