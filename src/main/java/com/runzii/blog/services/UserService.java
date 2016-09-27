package com.runzii.blog.services;

import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.runzii.blog.Constants;
import com.runzii.blog.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Inject
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @PostConstruct
    protected void initialize() {
        getSuperUser();
    }

    public com.runzii.blog.models.User createUser(com.runzii.blog.models.User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public com.runzii.blog.models.User getSuperUser(){
        com.runzii.blog.models.User user = userRepository.findByEmail(Constants.DEFAULT_ADMIN_EMAIL);

        if ( user == null) {
            user = createUser(new com.runzii.blog.models.User(Constants.DEFAULT_ADMIN_EMAIL, Constants.DEFAULT_ADMIN_PASSWORD, com.runzii.blog.models.User.ROLE_ADMIN));
        }

        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.runzii.blog.models.User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("user not found");
        }
        return createSpringUser(user);
    }

    public com.runzii.blog.models.User currentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || auth instanceof AnonymousAuthenticationToken){
            return null;
        }

        String email = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();

        return userRepository.findByEmail(email);
    }

    public boolean changePassword(com.runzii.blog.models.User user, String password, String newPassword){
        if (password == null || newPassword == null || password.isEmpty() || newPassword.isEmpty())
            return false;

        logger.info("" + passwordEncoder.matches(password, user.getPassword()));
        boolean match = passwordEncoder.matches(password, user.getPassword());
        if (!match)
            return false;

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("User @"+user.getEmail() + " changed password.");

        return true;
    }

    public void signin(com.runzii.blog.models.User user) {
        SecurityContextHolder.getContext().setAuthentication(authenticate(user));
    }

    private Authentication authenticate(com.runzii.blog.models.User user) {
        return new UsernamePasswordAuthenticationToken(createSpringUser(user), null, Collections.singleton(createAuthority(user)));
    }

    private org.springframework.security.core.userdetails.User createSpringUser(com.runzii.blog.models.User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(createAuthority(user)));
    }

    private GrantedAuthority createAuthority(com.runzii.blog.models.User user) {
        return new SimpleGrantedAuthority(user.getRole());
    }

}
