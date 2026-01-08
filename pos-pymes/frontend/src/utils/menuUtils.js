/**
 * Convierte una lista plana de menús en una estructura jerárquica (árbol).
 * @param {Array} menuList - La lista plana del backend
 * @returns {Array} - El árbol de menús
 */
export const buildMenuTree = (menuList) => {
  // 1. Crear un mapa rápido para acceso O(1)
  const map = {};
  menuList.forEach(item => {
    map[item.id] = { ...item, children: [] };
  });

  const tree = [];

  // 2. Construir el árbol
  menuList.forEach(item => {
    const node = map[item.id];
    if (item.parentId === null) {
      // Si no tiene padre, es raíz
      tree.push(node);
    } else {
      // Si tiene padre, buscar al padre y agregarle este como hijo
      const parent = map[item.parentId];
      if (parent) {
        parent.children.push(node);
      }
    }
  });

  // 3. Ordenar por el campo 'orden'
  const sortRecursive = (items) => {
    items.sort((a, b) => a.orden - b.orden);
    items.forEach(item => {
      if (item.children.length > 0) {
        sortRecursive(item.children);
      }
    });
  };

  sortRecursive(tree);
  return tree;
};