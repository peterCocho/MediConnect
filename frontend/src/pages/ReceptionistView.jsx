import React, { useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

const ReceptionistView = () => {
  const { user } = useAuth();

  useEffect(() => {
    console.log('User:', user);
  }, [user]);

  return (
    <div>
      <h1>Welcome to the Receptionist View</h1>
      <p>User: {user && user.name}</p>
    </div>
  );
};

export default ReceptionistView;