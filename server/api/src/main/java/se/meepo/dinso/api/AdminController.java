package se.meepo.dinso.api;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import se.meepo.dinso.database.entity.*;
import se.meepo.dinso.database.repository.ProfilePermissionRepository;
import se.meepo.dinso.database.repository.DemoProfileRepository;
import se.meepo.dinso.database.repository.CompanyRepository;
import se.meepo.dinso.service.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProfilePermissionRepository permissionRepository;
    private final DemoProfileRepository profileRepository;
    private final CompanyRepository companyRepository;

    public AdminController(ProfilePermissionRepository permissionRepository,
                          DemoProfileRepository profileRepository,
                          CompanyRepository companyRepository) {
        this.permissionRepository = permissionRepository;
        this.profileRepository = profileRepository;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/users")
    public List<UserSummary> getUsers() {
        return profileRepository.findAll().stream()
            .map(p -> new UserSummary(p.getExternalId(), p.getName(), p.getPortal().name()))
            .distinct()
            .sorted(Comparator.comparing(UserSummary::id))
            .toList();
    }

    @GetMapping("/users/{userId}/permissions")
    public UserPermissionsResponse getPermissionsForUser(@PathVariable("userId") String userId) {
        var profile = profileRepository.findByExternalId(userId);
        if (profile.isEmpty()) {
            return new UserPermissionsResponse(userId, Map.of());
        }
        
        var permissions = permissionRepository.findByProfile(profile.get());
        var companyMap = new HashMap<String, Map<String, String>>();
        
        for (var pp : permissions) {
            var permMap = companyMap.computeIfAbsent(
                pp.getCompany().getName(), 
                k -> new HashMap<String, String>());
            permMap.put(pp.getPermissionType().name(), pp.getLevel().name());
        }
        return new UserPermissionsResponse(userId, companyMap);
    }

    @PutMapping("/users/{userId}/permissions")
    @Transactional
    public void updatePermissions(@PathVariable("userId") String userId, 
                                  @RequestBody Map<String, Map<String, String>> newPermissions) {
        var profile = profileRepository.findByExternalId(userId);
        if (profile.isEmpty()) return;

        for (var entry : newPermissions.entrySet()) {
            var companyName = entry.getKey();
            var company = companyRepository.findByName(companyName);
            if (company.isEmpty()) continue;

            for (var permEntry : entry.getValue().entrySet()) {
                var existing = permissionRepository.findByProfileAndCompany(profile.get(), company.get()).stream()
                    .filter(pp -> pp.getPermissionType().name().equals(permEntry.getKey()))
                    .findFirst();
                try {
                    var permType = PermissionType.valueOf(permEntry.getKey());
                    var level = PermissionLevel.valueOf(permEntry.getValue());
                    if (existing.isPresent()) {
                        existing.get().setLevel(level);
                    } else {
                        permissionRepository.save(
                            new ProfilePermissionEntity(profile.get(), company.get(), permType, level));
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid enum value, skip
                }
            }
        }
    }

    public record UserSummary(String id, String name, String portal) {}
    
    public record UserPermissionsResponse(
        String userId,
        Map<String, Map<String, String>> companies
    ) {}
}