package com.pos.menu.repository;

import com.pos.menu.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Query("SELECT DISTINCT m FROM Menu m " +
           "JOIN RolMenu rm ON m.id = rm.menu.id " +
           "JOIN rm.role r WHERE r.name IN :roles " +
           "AND m.visible = true " +
           "ORDER BY m.sortOrder")
    List<Menu> findMenusByRoles(@Param("roles") Set<String> roles);
}
