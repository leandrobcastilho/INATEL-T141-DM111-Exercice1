package br.com.leandro.gae_exemplo1.service;

import br.com.leandro.gae_exemplo1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.logging.Logger;

import br.com.leandro.gae_exemplo1.model.User;

@Service("userDetailsService")
public class UserService implements UserDetailsService {

    private static final Logger log = Logger.getLogger("UserService");

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("UserService: loadUserByUsername - email " + email);
        Optional<User> optUser = userRepository.getByEmail(email);
        boolean isPresent = optUser.isPresent();
        if (isPresent) {
            log.info("UserService: loadUserByUsername - isPresent " + isPresent);
            //userRepository.updateUserLogin(optUser.get());
            return optUser.get();
        } else {
            log.info("UserService: Usuário não encontrado");
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
    }
}