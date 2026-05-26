package com.example.Bean.security;

import com.example.Bean.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final UserRepository userRepo;

    public CurrentUser(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /** Resolved user id of the authenticated principal, or null if anonymous. */
    public Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) return null;
        String email = auth.getName(); // formLogin maps usernameParameter("email") to name
        return userRepo.findByEmail(email).map(u -> u.getId()).orElse(null);
    }

    /** Same as id(), but throws if no user is authenticated. */
    public Long requireId() {
        Long id = id();
        if (id == null) {
            throw new IllegalStateException("No authenticated user");
        }
        return id;
    }

    public String email() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
    }
}
