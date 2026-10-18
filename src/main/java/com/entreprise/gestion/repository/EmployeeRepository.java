package com.entreprise.gestion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.entreprise.gestion.model.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    // Recherche par nom ou prénom
    List<Employee> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);

    // Recherche par département
    List<Employee> findByDepartement(String departement);

    // Liste avec diplômes
    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.diplomes WHERE e.id = :id")
    Optional<Employee> findByIdWithDiplomes(Long id);

    // Tous les employés avec leurs diplômes
    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.diplomes")
    List<Employee> findAllWithDiplomes();
}
