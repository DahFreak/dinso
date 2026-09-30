package se.meepo.dinso.database.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import se.meepo.dinso.database.entity.CompanyEntity;

public interface CompanyRepository extends JpaRepository<CompanyEntity, String> {
    Optional<CompanyEntity> findByName(String name);
}