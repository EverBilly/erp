package com.pos.role.repository;

import com.pos.role.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    List<Role> findByActiveTrue();

    @Query("SELECT r FROM Role r WHERE r.isSystem = true")
    List<Role> findSystemRoles();

    @Query("SELECT r FROM Role r WHERE r.priorityLevel > :minPriority AND r.active = true ORDER BY r.priorityLevel DESC")
    List<Role> findRolesWithMinPriority(@Param("minPriority") Integer minPriority);
}
