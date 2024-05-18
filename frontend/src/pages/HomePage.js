import React from 'react';

const HomePage = () => {
  return (
    <div className="home-page">
      <header style={{ backgroundColor: '#f0f0f0', padding: '20px 0' }}>
        <h1 style={{ fontSize: '32px', textAlign: 'center', color: '#333' }}>Welcome to Our Social Network</h1>
      </header>
      <main style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
        {/* What you can do section */}
        <section className="what-you-can-do">
          <h2 style={{ fontSize: '24px', marginBottom: '16px', textAlign: 'center' }}>What You Can Do</h2>
          <ul style={{ listStyleType: 'disc', marginLeft: '24px' }}>
            <li style={{ marginBottom: '8px' }}>Connect with friends and family</li>
            <li style={{ marginBottom: '8px' }}>Share updates, photos, and videos</li>
            <li style={{ marginBottom: '8px' }}>Discover trending topics and events</li>
            <li style={{ marginBottom: '8px' }}>Message and chat with friends in real-time</li>
            <li style={{ marginBottom: '8px' }}>Stay informed with personalized news feeds</li>
            <li style={{ marginBottom: '8px' }}>Customize your profile and privacy settings</li>
          </ul>
        </section>
      </main>
      <footer style={{ backgroundColor: '#333', color: '#fff', padding: '20px 0', textAlign: 'center' }}>
        <p>&copy; 2024 Social Network. All rights reserved.</p>
      </footer>
    </div>
  );
}

export default HomePage;
