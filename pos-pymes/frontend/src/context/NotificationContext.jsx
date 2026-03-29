import React, { useState, useCallback, useMemo } from 'react';
import { Snackbar, Alert } from '@mui/material';

const NotificationContext = React.createContext();

export const NotificationProvider = ({ children }) => {
  const [notification, setNotification] = useState({
    open: false,
    message: '',
    severity: 'success' // success, error, warning, info
  });

  const showNotification = useCallback((message, severity = 'success') => {
    setNotification({ open: true, message, severity });
  }, []); // Dependencias vacías = no recalcular jamás

  const hideNotification = useCallback(() => {
    setNotification(prev => ({ ...prev, open: false }));
  }, []);

  const value = useMemo(() => ({
    notification,
    showNotification,
    hideNotification
  }), [notification, showNotification, hideNotification]); // Solo recalcular si algo cambia de verdad

  return (
    <NotificationContext.Provider value={value}>
      {children}
      {notification.open && (
        <Snackbar
          open={notification.open}
          autoHideDuration={4000}
          onClose={hideNotification}
          anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
        >
          <Alert 
            onClose={hideNotification} 
            severity={notification.severity} 
            sx={{ width: '100%' }}
            variant="filled"
          >
            {notification.message}
          </Alert>
        </Snackbar>
      )}
    </NotificationContext.Provider>
  );
};

export const useNotification = () => {
  return React.useContext(NotificationContext);
};