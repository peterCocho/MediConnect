import React from 'react';
import styles from '../styles/Header.module.css';

const Header = () => {
  return (
    <header className={styles.header}>
      {/* Búsqueda Global */}
      <div className={styles.searchBarContainer}>
        <span className={styles.searchIcon}>🔍</span>
        <input 
          type="text" 
          placeholder="Búsqueda global..." 
          className={styles.searchInput}
        />
      </div>

      {/* Iconos y Perfil */}
      <div className={styles.headerActions}>
        <button className={styles.actionButton} aria-label="Notificaciones">🔔</button>
        <button className={styles.actionButton} aria-label="Mensajes">✉️</button>

        {/* Selector de Usuario */}
        <div className={styles.userDropdown}>
          <img src="/avatar.png" alt="Usuario" className={styles.avatar} /> {/* Reemplazar con la imagen real */}
          <div className={styles.userInfo}>
            <span className={styles.userName}>Usuario administrador</span>
            <span className={styles.role}>Administrador</span>
          </div>
          <span className={styles.dropdownArrow}>▼</span>
        </div>

        {/* Botón de Salida (Simulado) */}
        <button className={styles.logoutButton}>🚪</button>
      </div>
    </header>
  );
};

export default Header;
