import React from 'react';

function App() {
  return (
    <div style={{ padding: '20px', fontFamily: 'Arial' }}>
      <h1>🚀 Sistema POS - Frontend</h1>
      <p>El backend está funcionando en: <a href="http://localhost:8080">http://localhost:8080</a></p>
      
      <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#f0f0f0', borderRadius: '5px' }}>
        <h3>Servicios disponibles:</h3>
        <ul>
          <li><strong>Backend API</strong>: <a href="http://localhost:8080">localhost:8080</a></li>
          <li><strong>PostgreSQL</strong>: localhost:5432</li>
          <li><strong>pgAdmin</strong>: <a href="http://localhost:5050">localhost:5050</a></li>
        </ul>
      </div>
      
      <button 
        onClick={() => fetch('http://localhost:8080/health').then(r => r.text()).then(alert)}
        style={{ marginTop: '20px', padding: '10px 20px' }}
      >
        Probar conexión con Backend
      </button>
    </div>
  );
}

export default App;