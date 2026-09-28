package se.meepo.dinso.api;

import org.springframework.web.bind.annotation.*;
import se.meepo.dinso.service.*;

import java.util.*;
import se.meepo.dinso.service.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // In-memory storage for now (will be replaced with real implementation)
    private static final Map<String, Map<String, Map<PermissionType, PermissionLevel>>> permissionsData = new HashMap<>();
    
    // Mock data
    public AdminController() {
        // Initialize mock data
        // System admin user
        Map<PermissionType, PermissionLevel> adminPermissions = new HashMap<>();
        adminPermissions.put(PermissionType.LOGIN_COMPANY_PORTAL, PermissionLevel.READ);
        adminPermissions.put(PermissionType.APPROVE_CASES, PermissionLevel.WRITE);
        adminPermissions.put(PermissionType.ADD_EMPLOYEES, PermissionLevel.WRITE);
        adminPermissions.put(PermissionType.CHANGE_SALARY, PermissionLevel.WRITE);
        adminPermissions.put(PermissionType.REGISTER_LEAVE, PermissionLevel.WRITE);
        adminPermissions.put(PermissionType.TERMINATE_EMPLOYMENT, PermissionLevel.WRITE);
        
        permissionsData.put("system-admin-user", Map.of(
            "Nordljus Teknik AB", adminPermissions,
            "Horisont Konsult AB", adminPermissions
        ));
        
        // Regular admin user
        Map<PermissionType, PermissionLevel> regularAdminPermissions = new HashMap<>();
        regularAdminPermissions.put(PermissionType.LOGIN_COMPANY_PORTAL, PermissionLevel.READ);
        regularAdminPermissions.put(PermissionType.APPROVE_CASES, PermissionLevel.READ);
        regularAdminPermissions.put(PermissionType.ADD_EMPLOYEES, PermissionLevel.WRITE);
        regularAdminPermissions.put(PermissionType.CHANGE_SALARY, PermissionLevel.READ);
        regularAdminPermissions.put(PermissionType.REGISTER_LEAVE, PermissionLevel.READ);
        regularAdminPermissions.put(PermissionType.TERMINATE_EMPLOYMENT, PermissionLevel.READ);
        
        permissionsData.put("regular-admin-user", Map.of(
            "Nordljus Teknik AB", regularAdminPermissions,
            "Horisont Konsult AB", regularAdminPermissions
        ));
        
        // Regular user
        Map<PermissionType, PermissionLevel> regularUserPermissions = new HashMap<>();
        regularUserPermissions.put(PermissionType.LOGIN_COMPANY_PORTAL, PermissionLevel.READ);
        regularUserPermissions.put(PermissionType.APPROVE_CASES, PermissionLevel.NOT_ALLOWED);
        regularUserPermissions.put(PermissionType.ADD_EMPLOYEES, PermissionLevel.NOT_ALLOWED);
        regularUserPermissions.put(PermissionType.CHANGE_SALARY, PermissionLevel.NOT_ALLOWED);
        regularUserPermissions.put(PermissionType.REGISTER_LEAVE, PermissionLevel.NOT_ALLOWED);
        regularUserPermissions.put(PermissionType.TERMINATE_EMPLOYMENT, PermissionLevel.NOT_ALLOWED);
        
        permissionsData.put("regular-user", Map.of(
            "Nordljus Teknik AB", regularUserPermissions,
            "Horisont Konsult AB", regularUserPermissions
        ));
    }

    @GetMapping("/users")
    public List<String> getUsers() {
        return new ArrayList<>(permissionsData.keySet());
    }

    @GetMapping("/users/{userId}/permissions")
    public Map<String, Map<String, Map<PermissionType, PermissionLevel>>> getPermissionsForUser(@PathVariable String userId) {
        return Map.of(userId, permissionsData.getOrDefault(userId, new HashMap<>()));
    }

    @PutMapping("/users/{userId}/permissions")
    public void updatePermissions(@PathVariable String userId, 
                                  @RequestBody Map<String, Map<PermissionType, PermissionLevel>> newPermissions) {
        permissionsData.put(userId, newPermissions);
    }
}