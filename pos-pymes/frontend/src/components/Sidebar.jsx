import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import * as AiIcons from 'react-icons/ai'; // Ant Design Icons (muy bonitos)
import { useAuth } from '../context/AuthContext';

// Helper para mapear el string del backend al Icono de React
const getIcon = (iconName) => {
    // Mapeo manual para los iconos que tienes en la DB
    // Puedes agregar más según aparezcan en tu DB
    const iconMap = {
        'home': AiIcons.AiFillHome,
        'settings': AiIcons.AiFillSetting,
        'users': AiIcons.AiFillContacts,
        'shield': AiIcons.AiFillSafety,
        'key': AiIcons.AiFillKey,
        'menu': AiIcons.AiFillMenu,
        'sliders': AiIcons.AiFillControl,
        'activity': AiIcons.AiFillAudio,
        'bar-chart': AiIcons.AiFillBarChart,
        'user': AiIcons.AiFillUser,
        'lock': AiIcons.AiFillLock,
        'monitor': AiIcons.AiFillMonitor,
        'list': AiIcons.AiFillFileText,
        // Fallback por si falta alguno
        'default': AiIcons.AiFillAppstore
    };
    const IconComponent = iconMap[iconName] || iconMap['default'];
    return <IconComponent size={20} className="mr-2" />;
};

const MenuItem = ({ item, depth = 0 }) => {
    const location = useLocation();
    const isActive = location.pathname === item.ruta;
    
    // Estilos dinámicos
    const activeClass = isActive ? 'bg-blue-600 text-white' : 'text-gray-300 hover:bg-gray-700';
    const paddingLeft = depth * 16 + 12; // Indentación para hijos

    // Si tiene hijos, renderizamos recursivamente
    if (item.children && item.children.length > 0) {
        return (
            <div>
                <div className={`flex items-center px-4 py-3 text-sm font-medium cursor-pointer ${activeClass}`} style={{ paddingLeft }}>
                    {getIcon(item.icono)}
                    <span>{item.nombre}</span>
                    <span className="ml-auto text-xs">▼</span>
                </div>
                {/* Renderizar hijos recursivamente */}
                <div className="bg-gray-800">
                    {item.children.map(child => (
                        <MenuItem key={child.id} item={child} depth={depth + 1} />
                    ))}
                </div>
            </div>
        );
    }

    // Si es hoja (sin hijos)
    return (
        <Link to={item.ruta} className="block text-decoration-none">
            <div className={`flex items-center px-4 py-3 text-sm font-medium transition-colors cursor-pointer ${activeClass}`} style={{ paddingLeft }}>
                {getIcon(item.icono)}
                <span>{item.nombre}</span>
            </div>
        </Link>
    );
};

const Sidebar = () => {
    const { menuTree } = useAuth();

    return (
        <div className="w-64 bg-gray-900 text-white h-screen fixed left-0 top-0 flex flex-col shadow-xl">
            <div className="h-16 flex items-center justify-center border-b border-gray-700">
                <h1 className="text-xl font-bold tracking-wider">POS SaaS</h1>
            </div>
            
            <nav className="flex-1 overflow-y-auto py-4">
                {menuTree && menuTree.length > 0 ? (
                    menuTree.map(item => (
                        <MenuItem key={item.id} item={item} />
                    ))
                ) : (
                    <p className="px-4 text-gray-500 text-sm">Cargando menú...</p>
                )}
            </nav>

            <div className="p-4 border-t border-gray-700 text-xs text-center text-gray-500">
                © 2024 Tu Empresa
            </div>
        </div>
    );
};

export default Sidebar;