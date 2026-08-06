import React from 'react';
import styles from '../styles/Sidebar.module.css';

const Sidebar = () => {
  const navItems = [
    { name: 'Salpicadero', icon: '🏠', page: '/dashboard' },
    { name: 'Gestión de Usuarios', icon: '👥', page: '/users' }, // Activo
    { name: 'Configuración', icon: '⚙️', page: '/settings' },
    { name: 'Citas Global', icon: '📅', page: '/appointments' },
    { name: 'Informes', icon: '📊', page: '/reports' },
  ];

  return (
    <div className={styles.sidebar}>
      <div className={styles.logoContainer}>
        <span role="img" aria-label="Logo">🏥</span>
        <h1 className={styles.appName}>MediConnect</h1>
      </div>
      <nav className={styles.navMenu}>
        {navItems.map((item) => (
          <div 
            key={item.name} 
            className={`${styles.navItem} ${item.page === '/users' ? styles.active : ''}`}
            onClick={() => console.log(`Navegando a: ${item.page}`)}
          >
            <span className={styles.icon}>{item.icon}</span>
            <span>{item.name}</span>
          </div>
        ))}
      </nav>
      <div className={styles.footerInfo}>
        MediConnect v1.0
      </div>
    </div>
  );
};

export default Sidebar;
