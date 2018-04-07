package br.com.leandro.gae_exemplo1.service;

import br.com.leandro.gae_exemplo1.model.User;
import br.com.leandro.gae_exemplo1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.logging.Logger;

@Service("clientDetailsService")
public class ClientService implements ClientDetailsService {

    private static final Logger log = Logger.getLogger("ClientService");

    @Override
    public ClientDetails loadClientByClientId(String s) throws ClientRegistrationException {
        return null;
    }

//    @Autowired
//    private UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        log.info("UserService: loadUserByUsername - email " + email);
//        Optional<User> optUser = userRepository.getByEmail(email);
//        boolean isPresent = optUser.isPresent();
//        if (isPresent) {
//            log.info("UserService: loadUserByUsername - isPresent " + isPresent);
//            userRepository.updateUserLogin(optUser.get());
//            return optUser.get();
//        } else {
//            log.info("UserService: Usuário não encontrado");
//            throw new UsernameNotFoundException("Usuário não encontrado");
//        }
//    }
}