import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
    Box,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Divider,
    Collapse,
    Typography,
    IconButton,
    Tooltip
} from '@mui/material';
import { ExpandLess, ExpandMore } from '@mui/icons-material';
import { getIconComponent } from '../utils/iconMapper';

const MenuItem = ({ item, depth = 0 }) => {
    const location = useLocation();
    const [open, setOpen] = React.useState(false);
    const isLeaf = !item.children || item.children.length === 0;

    const isActive = location.pathname === item.ruta;
    
    const handleClick = () => {
        if (!isLeaf) {
            // Navegar si es hoja
        } else {
            // Si no es hoja, expandir/colapsar
            setOpen(!open);
        }
    };
    
    const paddingLeft = depth * 16;
    const IconComponent = getIconComponent(item.icono);

  return (
    <>
      <ListItem disablePadding>
        <ListItemButton
          component={Link}
          to={item.ruta}
          onClick={handleClick}
          sx={{
            pl: `${paddingLeft}px`,
            backgroundColor: isActive ? 'primary.main' : 'transparent',
            color: isActive ? 'white' : 'inherit',
            '&:hover': {
              backgroundColor: isActive ? 'primary.dark' : 'rgba(0, 0, 0, 0.04)',
              color: isActive ? 'white' : 'text.primary',
            },
            // Efecto de borde izquierdo al activo
            '&::before': {
              content: '""',
              position: 'absolute',
              left: 0,
              top: '50%',
              transform: 'translateY(-50%)',
              width: '4px',
              height: '24px',
              bgcolor: isActive ? 'primary.contrastText' : 'transparent',
              opacity: isActive ? 1 : 0,
              transition: 'opacity 0.3',
            }
          }}
        >
          <ListItemIcon sx={{ color: isActive ? 'white' : 'inherit' }}>
            <IconComponent fontSize="small" />
          </ListItemIcon>
          <ListItemText primary={item.nombre} sx={{ whiteSpace: 'normal' }} />
          {!isLeaf && (open ? <ExpandLess /> : <ExpandMore />)}
        </ListItemButton>
      </ListItem>
      
      {!isLeaf && (
        <Collapse in={open} timeout="auto" unmountOnExit>
          <List component="div" disablePadding>
            {item.children.map((child) => (
              <MenuItem key={child.id} item={child} depth={depth + 1} />
            ))}
          </List>
        </Collapse>
      )}
    </>
  );
};

const Sidebar = () => {
  const { menuTree, loading, user } = useAuth();

  if (loading) {
    return (
      <Box sx={{ width: 256, bgcolor: 'background.paper', height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Typography>Cargando...</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ width: 256, bgcolor: 'background.paper', height: '100vh', borderRight: '1px solid', borderColor: 'divider' }}>
      <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Typography variant="h6" noWrap>
          POS System
        </Typography>
      </Box>
      
      <List sx={{ py: 0 }}>
        {menuTree && menuTree.length > 0 ? (
          menuTree.map((item) => (
            <MenuItem key={item.id} item={item} />
          ))
        ) : (
          <ListItem>
            <ListItemText primary="Sin acceso a módulos" secondary="Contacte al administrador." />
          </ListItem>
        )}
      </List>

      <Divider sx={{ mt: 'auto', mb: 0 }} />

      <Box sx={{ p: 2, borderTop: '1px solid', borderColor: 'divider' }}>
        <Typography variant="caption" color="text.secondary" align="center">
          © 2024 Tu Empresa
        </Typography>
      </Box>
    </Box>
  );
};

export default Sidebar;