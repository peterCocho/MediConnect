import React from 'react';
import Sidebar from './Sidebar';
import Header from './Header';
import UserTable from './UserTable';
import styles from '../styles/App.module.css';

const Layout = () => {
  return (
    <div className={styles.appContainer}>
      {/* 1. Barra Lateral */}
      <aside className={styles.sidebarWrapper}>
        <Sidebar />
      </aside>

      {/* 2. Contenido Principal */}
      <main className={styles.mainContent}>
        {/* Encabezado Superior (Búsqueda, Perfil) */}
        <Header />
        
        {/* Área de contenido real: Gestión de Usuarios */}
        <div className={styles.contentArea}>
          <h1>Gestión de Usuarios</h1>
          <p>Administre el personal y permisos del sistema</p>
          
          {/* La tabla que consume la API */}
          <UserTable />
        </div>
      </main>
    </div>
  );
};

export default Layout;
