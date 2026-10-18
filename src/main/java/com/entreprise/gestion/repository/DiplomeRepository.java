package com.entreprise.gestion.repository;

import com.entreprise.gestion.model.Diplome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiplomeRepository extends JpaRepository<Diplome, Long> {

    List<Diplome> findByEmployeeId(Long employeeId);

    void deleteByEmployeeId(Long employeeId);
}
