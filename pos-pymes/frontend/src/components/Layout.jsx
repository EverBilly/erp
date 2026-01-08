import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  AppBar,
  Box,
  Drawer,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  Avatar,
  Menu,
  MenuItem,
  Divider,
  Collapse,
  ListItemButton
} from '@mui/material';
import {
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  PointOfSale as PosIcon,
  Inventory as InventoryIcon,
  People as PeopleIcon,
  Assessment as AssessmentIcon,
  Settings as SettingsIcon,
  Logout as LogoutIcon,
  Person as PersonIcon,
  Security as SecurityIcon,
  Lock as LockIcon,
  Key as KeyIcon,
  Monitor as MonitorIcon,
  Description as DescriptionIcon,
  ExpandLess,
  ExpandMore,
} from '@mui/icons-material';

const drawerWidth = 240;

// --- COMPONENTE RECURSIVO PARA SUBMENÚS ---
const SidebarItem = ({ item, depth = 0, onNavigate, currentPath }) => {
  const [open, setOpen] = useState(false);
  const isLeaf = !item.children || item.children.length === 0;

  // Mapeo de nombres de BD a Componentes de Iconos MUI
  const getIcon = (iconName) => {
    const iconMap = {
        'home': <DashboardIcon />,
        'dashboard': <DashboardIcon />,
        'settings': <SettingsIcon />,
        'sliders': <SettingsIcon />,
        'users': <PeopleIcon />,
        'shield': <SecurityIcon />,
        'key': <KeyIcon />,
        'menu': <MenuIcon />,
        'activity': <AssessmentIcon />,
        'bar-chart': <AssessmentIcon />,
        'user': <PersonIcon />,
        'lock': <LockIcon />,
        'monitor': <MonitorIcon />,
        'list': <DescriptionIcon />,
        'default': <DashboardIcon />
    };
    // Si el icono no existe, muestra el default
    return iconMap[iconName] || iconMap['default'];
  };

  const handleClick = () => {
    if (isLeaf) {
      onNavigate(item.ruta);
    } else {
      setOpen(!open);
    }
  };

  // Estilo visual para item activo
  const isSelected = currentPath === item.ruta;
  
  // Indentación para submenús
  const paddingLeft = 16 + (depth * 16);

  return (
    <>
      <ListItem disablePadding>
        <ListItemButton 
          onClick={handleClick}
          sx={{ 
            pl: `${paddingLeft}px`,
            backgroundColor: isSelected ? 'primary.main' : 'transparent',
            color: isSelected ? 'white' : 'inherit',
            '&:hover': {
              backgroundColor: isSelected ? 'primary.dark' : 'rgba(0, 0, 0, 0.04)'
            }
          }}
        >
          <ListItemIcon sx={{ color: isSelected ? 'white' : 'inherit' }}>
            {getIcon(item.icono)}
          </ListItemIcon>
          <ListItemText primary={item.nombre} sx={{ whiteSpace: 'normal' }} />
          {!isLeaf ? (open ? <ExpandLess /> : <ExpandMore />) : null}
        </ListItemButton>
      </ListItem>
      
      {/* Renderizar hijos recursivamente si existen */}
      {!isLeaf && (
        <Collapse in={open} timeout="auto" unmountOnExit>
          <List disablePadding>
            {item.children.map((child) => (
              <SidebarItem 
                key={child.id} 
                item={child} 
                depth={depth + 1} 
                onNavigate={onNavigate}
                currentPath={currentPath}
              />
            ))}
          </List>
        </Collapse>
      )}
    </>
  );
};
// -------------------------------------------------

const Layout = ({ children }) => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState(null);
  const { user, logout, menuTree } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleMenuOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
    handleMenuClose();
  };

  // Contenido del Drawer (ahora dinámico desde el Contexto)
  const drawer = (
    <div>
      <Toolbar>
        <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
          POS System
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {menuTree && menuTree.length > 0 ? (
          menuTree.map((item) => (
            <SidebarItem 
              key={item.id} 
              item={item} 
              onNavigate={(path) => navigate(path)}
              currentPath={location.pathname}
            />
          ))
        ) : (
          <ListItem>
            <ListItemText primary="Cargando menú..." />
          </ListItem>
        )}
      </List>
    </div>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar
        position="fixed"
        sx={{
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          ml: { sm: `${drawerWidth}px` },
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            {user?.nombreCompleto || 'Dashboard'}
          </Typography>
          
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <Typography variant="body2" sx={{ mr: 2, display: { xs: 'none', sm: 'block' } }}>
              {user?.nombreCompleto}
            </Typography>
            <IconButton
              size="large"
              onClick={handleMenuOpen}
              color="inherit"
            >
              <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main' }}>
                <PersonIcon fontSize="small" />
              </Avatar>
            </IconButton>
            <Menu
              anchorEl={anchorEl}
              open={Boolean(anchorEl)}
              onClose={handleMenuClose}
            >
              <MenuItem onClick={() => navigate('/perfil')}>
                <ListItemIcon>
                  <PersonIcon fontSize="small" />
                </ListItemIcon>
                <ListItemText>Perfil</ListItemText>
              </MenuItem>
              <Divider />
              <MenuItem onClick={handleLogout}>
                <ListItemIcon>
                  <LogoutIcon fontSize="small" />
                </ListItemIcon>
                <ListItemText>Cerrar Sesión</ListItemText>
              </MenuItem>
            </Menu>
          </Box>
        </Toolbar>
      </AppBar>
      
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
      >
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true, // Better open performance on mobile.
          }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          mt: 8
        }}
      >
        {children}
      </Box>
    </Box>
  );
};

export default Layout;