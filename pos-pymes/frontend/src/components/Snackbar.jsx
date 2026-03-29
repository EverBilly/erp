import React from 'react';
import { Snackbar, Alert } from '@mui/material';
import { useNotification } from '../context/NotificationContext';

const CustomSnackbar = () => {
  const { notification, hideNotification } = useNotification();

  return (
    <Snackbar
      open={notification.open}
      autoHideDuration={4000}
      onClose={hideNotification}
      anchorOrigin={{ vertical: 'top', horizontal: 'right' }} // Esquina superior derecha
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
  );
};

export default CustomSnackbar;