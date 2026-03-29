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
    Tooltip,
    Avatar
} from '@mui/material';
import { ExpandLess, ExpandMore } from '@mui/icons-material';
import { getIconComponent } from '../utils/iconMapper';

const MenuItem = ({ item, depth = 0 }) => {
    const location = useLocation();
    const [open, setOpen] = React.useState(false);
    const isLeaf = !item.children || item.children.length === 0;

    const isActive = location.pathname === item.path;
    
    const handleClick = () => {
        if (!isLeaf) {
          setOpen(!open);
        }
    };
    
    const paddingLeft = 24 + (depth * 16);
    const IconComponent = getIconComponent(item.icon);

  return (
    <>
      <ListItem disablePadding>
        <ListItemButton
          component={Link}
          to={item.path}
          onClick={handleClick}
          sx={{
            pl: `${paddingLeft}px`,
            backgroundColor: isActive ? 'primary.main' : 'transparent',
            color: isActive ? 'white' : 'inherit',
            '&:hover': {
              backgroundColor: isActive ? 'primary.dark' : 'action.hover',
              color: isActive ? 'white' : 'text.primary',
            },
            borderRadius: 2,
            mx: 1,
            mb: 0.5,
          }}
        >
          <ListItemIcon sx={{ color: isActive ? 'white' : 'inherit' }}>
            <IconComponent fontSize="small" />
          </ListItemIcon>
          <ListItemText 
            primary={item.name}
            sx={{ 
              whiteSpace: 'normal',
              overflow: 'hidden',
              textOverflow: 'ellipsis'
            }} 
          />
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

  // Obtener el rol del usuario de forma más robusta
  const getUserRole = () => {
    if (user?.rol) return user.rol;
    if (user?.role) return user.role;
    if (user?.roles && Array.isArray(user.roles) && user.roles.length > 0) {
      return user.roles[0].authority || user.roles[0].name;
    }
    return 'Rol no definido';
  };

  const userRole = getUserRole();

  return (
    <Box sx={{ width: 256, bgcolor: 'background.paper', height: '100vh', borderRight: '1px solid', borderColor: 'divider' }}>
      <Box 
        sx={{ 
          p: 2, 
          borderBottom: '1px solid', 
          borderColor: 'divider',
          bgcolor: 'primary.light',
          color: 'white'
          }}
      >
        {/* <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
          <Avatar sx={{ bgcolor: 'primary.main', width: 40, height: 40 }}>
            {user?.fullName?.charAt(0)?.toUpperCase() || user?.username?.charAt(0)?.toUpperCase() || 'U'}
          </Avatar>
          <Box>
            <Typography variant="h6" noWrap sx={{ fontWeight: 600 }}>
              {user?.fullName || user?.username || 'Usuario'}
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.9 }}>
              {userRole}
            </Typography>
          </Box>
        </Box> */}
      </Box>
      
      <List sx={{ py: 0 }}>
        {menuTree && menuTree.length > 0 ? (
          menuTree.map((item) => (
            <MenuItem key={item.id} item={item} />
          ))
        ) : (
          <ListItem>
            <ListItemText 
              primary="Sin acceso a módulos" 
              secondary="Contacte al administrador." 
              sx={{ textAlign: 'center' }}/>
          </ListItem>
        )}
      </List>

      <Divider sx={{ mt: 'auto', mb: 0 }} />

      <Box sx={{ p: 2, borderTop: '1px solid', borderColor: 'divider' }}>
        <Typography variant="caption" color="text.secondary" align="center">
          © 2026 POS SYSTEM
        </Typography>
      </Box>
    </Box>
  );
};

export default Sidebar;