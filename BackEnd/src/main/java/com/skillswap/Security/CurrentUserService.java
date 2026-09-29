package com.skillswap.Security;

import com.skillswap.Entity.User;
import com.skillswap.Exception.UserNotFoundException;
import com.skillswap.Repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("currentUserService")
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal())) {
            throw new AccessDeniedException("Not authenticated");
        }
        return userRepository.findByEmail(a.getName())
                .orElseThrow(() -> new UserNotFoundException(a.getName()));
    }

    public boolean isSelf(Integer userId) {
        return getCurrentUser().getId().equals(userId);
    }
}