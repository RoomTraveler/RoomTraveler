package com.ssafy.trip.security;

import com.ssafy.trip.user.UserDao;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.ssafy.trip.user.User user = userDao.getUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Could not found user" + username));

        return new CustomUserDetails(user);
    }
}
