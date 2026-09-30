package se.meepo.dinso.database.entity;

import jakarta.persistence.*;
import se.meepo.dinso.service.*;

@Entity
@Table(
    name = "profile_permission",
    uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "company_id", "permission_type"}))
public class ProfilePermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    private PermissionType permissionType;

    @Column(name = "level", nullable = false)
    @Enumerated(EnumType.STRING)
    private PermissionLevel level;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_profile_permission_profile"))
    private DemoProfileEntity profile;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_profile_permission_company"))
    private CompanyEntity company;

    protected ProfilePermissionEntity() {}

    public ProfilePermissionEntity(DemoProfileEntity profile, CompanyEntity company, PermissionType permissionType, PermissionLevel level) {
        this.profile = profile;
        this.company = company;
        this.permissionType = permissionType;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public PermissionLevel getLevel() {
        return level;
    }

    public DemoProfileEntity getProfile() {
        return profile;
    }

    public CompanyEntity getCompany() {
        return company;
    }

    public void setProfile(DemoProfileEntity profile) {
        this.profile = profile;
    }

    public void setLevel(String level) {
        this.level = PermissionLevel.valueOf(level.toUpperCase());
    }

    public void setLevel(PermissionLevel level) {
        this.level = level;
    }

    public void setProfileId(String profileId) {
        // This is a helper method for the service layer
        // The actual profile entity reference should be set separately
    }

    public void setCompanyId(String companyId) {
        // This is a helper method for the service layer
        // The actual company entity reference should be set separately
    }
}