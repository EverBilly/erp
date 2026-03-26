package com.pos.permission.repository;

import com.pos.permission.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    List<Permission> findByModule(String module);

    List<Permission> findByCategory(String category);

    List<Permission> findByActiveTrue();

    @Query("SELECT p FROM Permission p WHERE p.code IN :codes AND p.active = true")
    List<Permission> findByCodes(@Param("codes") Set<String> codes);
}
