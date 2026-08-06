import React, { useState, useEffect } from 'react';
import styles from '../styles/Table.module.css';

// Datos simulados que vendrían del GET /api/users de Spring Boot
const initialUsers = [
    { id: 1, nombre: "Dr. Carlos Ramírez", documento: "1234567890", rol: "Médico", especialidad: "Cardiología", estado: "Activo", acciones: "Editar" },
    { id: 2, nombre: "Dra. María López", documento: "0987654321", rol: "Médico", especialidad: "Medicina General", estado: "Activo", acciones: "Editar" },
    { id: 3, nombre: "Dr. José García", documento: "1122334455", rol: "Médico", especialidad: "Dermatología", estado: "Activo", acciones: "Editar" },
    { id: 4, nombre: "Ana Martínez", documento: "5544332211", rol: "Recepcionista", especialidad: "N/A", estado: "Activo", acciones: "Editar" },
    { id: 5, nombre: "Luis Fernández", documento: "6677889900", rol: "Administrador", especialidad: "N/A", estado: "Inactivo", acciones: "Editar" },
];

const UserTable = () => {
  const [users, setUsers] = useState(initialUsers);
  const [loading, setLoading] = useState(true);

  // *** SIMULACIÓN DE CONSUMO DE API ***
  useEffect(() => {
    // Aquí iría el fetch o axios.get('/api/users')
    setTimeout(() => {
      setUsers(initialUsers); // Simula la respuesta exitosa del backend
      setLoading(false);
    }, 500);
  }, []);

  const handleEdit = (user) => {
    console.log(`Abriendo edición para: ${user.nombre}`);
    // Aquí se abriría un modal o navegaría a /users/{id}/edit
  };

  return (
    <div className={styles.container}>
      {/* Botón de Nuevo Usuario */}
      <button 
        className={styles.addButton} 
        onClick={() => console.log("Crear nuevo usuario")}
      >
        ➕ Nuevo Usuario
      </button>

      {loading ? (
        <div className={styles.loading}>Cargando usuarios...</div>
      ) : (
        <table className={styles.userTable}>
          <thead>
            <tr>
              <th>NOMBRE</th>
              <th>DOCUMENTO</th>
              <th>ROL</th>
              <th>ESPECIALIDAD</th>
              <th>ESTADO</th>
              <th>ACCIONES</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} className={styles.row}>
                <td>{user.nombre}</td>
                <td>{user.documento}</td>
                <td>{user.rol}</td>
                <td>{user.especialidad}</td>
                <td className={`${styles.estadoCell} ${user.estado === 'Activo' ? styles.activo : styles.inactivo}`}>{user.estado}</td>
                <td>
                  <button 
                    className={styles.actionButton} 
                    onClick={() => handleEdit(user)}
                  >
                    Editar
                  </button>
                  {/* Aquí se podría añadir otro botón o icono */}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default UserTable;
