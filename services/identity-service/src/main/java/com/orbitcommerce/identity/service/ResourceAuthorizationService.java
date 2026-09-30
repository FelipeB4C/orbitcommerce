package com.orbitcommerce.identity.service;

import com.orbitcommerce.identity.model.User;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
public class ResourceAuthorizationService {

    private final RoleHierarchy roleHierarchy;

    public ResourceAuthorizationService(RoleHierarchy roleHierarchy) {
        this.roleHierarchy = roleHierarchy;
    }

    // MÉTODO DE ANTES — genérico, aceita qualquer role como parâmetro
    public boolean canAccess(User authenticated, UUID resourceOwnerId, String requiredRole) {
        if (authenticated.getId().equals(resourceOwnerId)) {
            return true;
        }

        // 2. Possui a role exigida (direta ou herdada via hierarquia)?
        Collection<? extends GrantedAuthority> reachableAuthorities =
                roleHierarchy.getReachableGrantedAuthorities(authenticated.getAuthorities());

        return reachableAuthorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(requiredRole));
    }

    // MÉTODO NOVO — adicionado na resposta anterior, específico pra deleção
    public boolean canDelete(User authenticated, UUID targetUserId) {
        boolean isSelf = authenticated.getId().equals(targetUserId);
        boolean isAdmin = authenticated.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isSelf || isAdmin;
    }

}
