package com.orbitcommerce.identity.security;

import com.orbitcommerce.identity.model.User;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HierarchyService {

    private final RoleHierarchy roleHierarchy;

    public HierarchyService(RoleHierarchy roleHierarchy) {
        this.roleHierarchy = roleHierarchy;
    }

    public boolean verifyIfUserCanDelete(User userLoggedIn, User userToDelete, String desiredRole) {

        if (userLoggedIn.getId().equals(userToDelete.getId())) {
            return true;
        }

        for (GrantedAuthority grantedAuthority : userLoggedIn.getAuthorities()) {
            var reachableAuthorities = roleHierarchy.getReachableGrantedAuthorities(List.of(grantedAuthority));

            for (GrantedAuthority reachable : reachableAuthorities) {
                if (reachable.getAuthority().equals(desiredRole)) {
                    return true;
                }
            }
        }
        return false;
    }

}
