package se.meepo.dinso.database.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import se.meepo.dinso.database.entity.*;

public interface ProfilePermissionRepository
    extends JpaRepository<ProfilePermissionEntity, String> {
  List<ProfilePermissionEntity> findByProfile(DemoProfileEntity profile);
  List<ProfilePermissionEntity> findByProfileAndCompany(DemoProfileEntity profile, CompanyEntity company);
}
