package com.testtask.taskmanagement.config.security;

import com.testtask.taskmanagement.model.User;
import com.testtask.taskmanagement.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityUserDetailsService implements UserDetailsService {
    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User currentUser = usersRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Username \"" + username + "\" not found.")
        );

        return org.springframework.security.core.userdetails.User.builder()
                .username(currentUser.getUsername())
                .password(currentUser.getPassword())
                .authorities(currentUser.getRole().name())
                .build();
    }
}
