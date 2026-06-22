import React, { useContext } from 'react';

const AuthContext = React.createContext({
  user: null,
  setUser: () => {},
  isLoading: false,
  loading: false,
});

export const useAuth = () => useContext(AuthContext);

export default AuthContext;