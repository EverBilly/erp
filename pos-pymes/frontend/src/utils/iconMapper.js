import {
  Home as HomeIcon,
  People as PeopleIcon,
  Settings as SettingsIcon,
  Security as SecurityIcon,
  Lock as LockIcon,
  Menu as MenuIcon,
  Assessment as AssessmentIcon,
  Person as PersonIcon,
  Monitor as MonitorIcon,
  List as ListIcon,
  Dashboard as DashboardIcon,
  AdminPanelSettings as AdminPanelIcon,
  Assignment as AssignmentIcon,
  BarChart as BarChartIcon,
  Inventory as InventoryIcon,
  ShoppingCart as ShoppingCartIcon,
  Receipt as ReceiptIcon,
  Build as BuildIcon,
  History as HistoryIcon,
  Report as ReportIcon,
  VerifiedUser as VerifiedUserIcon,
  Key as KeyIcon,
  Computer as ComputerIcon,
  Description as DescriptionIcon,
  Tune as TuneIcon,
  Rule as RuleIcon,
  LibraryBooks as LibraryBooksIcon,
  Analytics as AnalyticsIcon,
  SettingsApplications as SettingsApplicationsIcon,
} from '@mui/icons-material';

export const getIconComponent = (iconName) => {
  const iconMap = {
    // Básicos
    'home': HomeIcon,
    'dashboard': DashboardIcon,
    'users': PeopleIcon,
    'person': PersonIcon,
    'settings': SettingsIcon,
    'admin_panel_settings': AdminPanelIcon,
    'assignment': AssignmentIcon,
    'bar-chart': BarChartIcon,
    'inventory': InventoryIcon,
    'shopping_cart': ShoppingCartIcon,
    'receipt': ReceiptIcon,
    'build': BuildIcon,
    'history': HistoryIcon,
    'report': ReportIcon,
    'verified_user': VerifiedUserIcon,
    'key': KeyIcon,
    'lock': LockIcon,
    'monitor': MonitorIcon,
    'list': ListIcon,
    'menu': MenuIcon,
    'activity': AssessmentIcon,
    'shield': SecurityIcon,
    'computer': ComputerIcon,
    'description': DescriptionIcon,
    'tune': TuneIcon,
    'rule': RuleIcon,
    'library_books': LibraryBooksIcon,
    'analytics': AnalyticsIcon,
    'settings_applications': SettingsApplicationsIcon,

    // Fallback
    'default': PersonIcon,
  };

  const Icon = iconMap[iconName] || iconMap['default'];
  return Icon;
};

// Para usar en Material UI: <Icon fontSize="large" />