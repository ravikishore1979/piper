package com.creactiviti.piper.workflows.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
public class PiperUserDetailService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String presentedUser) throws UsernameNotFoundException {

        if(!"user1".equals(presentedUser)) {
            String errorMsg = String.format("Invalid User '%s'", presentedUser);
            UsernameNotFoundException ufe = new UsernameNotFoundException(errorMsg);
            log.error(errorMsg, ufe);
            throw ufe;
        }
        return new User("user1", "password", new ArrayList<>());
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
