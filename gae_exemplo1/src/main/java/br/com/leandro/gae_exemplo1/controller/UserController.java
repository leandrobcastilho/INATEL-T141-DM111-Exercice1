package br.com.leandro.gae_exemplo1.controller;

import br.com.leandro.gae_exemplo1.exception.UserAlreadyExistsException;
import br.com.leandro.gae_exemplo1.exception.UserNotFoundException;
import br.com.leandro.gae_exemplo1.model.User;
import br.com.leandro.gae_exemplo1.repository.UserRepository;
import br.com.leandro.gae_exemplo1.util.CheckRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping(path = "/api/users")
public class UserController {

    private static final Logger log = Logger.getLogger("UserController");

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasAuthority('" + CheckRole.ROLE_ADMIN + "')")
    @GetMapping
    public List<User> getUsers() {
        log.info("UserController: getUsers");
        return userRepository.getUsers();
    }

    @PreAuthorize("hasAuthority('" + CheckRole.ROLE_ADMIN + "')")
    @GetMapping(path = "/createbyemail")
    public List<User> getUsersCreateByEmail(@RequestParam("email") String email) {
        log.info("UserController: getUsersCreateByEmail - email");
        return userRepository.getUsersCreateByEmail(email);
    }

    @PreAuthorize("hasAnyAuthority('" + CheckRole.ROLE_ADMIN + "', '" + CheckRole.ROLE_INIT + "')")
    @PostMapping
    public ResponseEntity<User> saveUser(@RequestBody User user, Authentication authentication) {
        log.info("UserController: saveUser");
        if(user.getRole().equalsIgnoreCase(CheckRole.ROLE_INIT)){
            log.info("UserController: saveUser - FORBIDDEN ");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            boolean hasRoleInit = CheckRole.hasRoleInit(authentication);
            log.info("UserController: saveUser - hasRoleInit " + hasRoleInit);
            if (hasRoleInit) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                Optional<User> optUser = userRepository.getByOwnerEmail(userDetails.getUsername());
                boolean isPresent = optUser.isPresent();
                log.info("UserController: saveUser - isPresent " + isPresent);
                if (isPresent) {
                    log.info("UserController: saveUser - FORBIDDEN ");
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                } else {
                    log.info("UserController: saveUser - setRole - " + CheckRole.ROLE_ADMIN);
                    log.info("UserController: saveUser - setOwnerEmail - " + userDetails.getUsername());
                    user.setRole(CheckRole.ROLE_ADMIN);
                    user.setOwnerEmail(userDetails.getUsername());
                }
            }
            log.info("UserController: saveUser - OK ");
            return new ResponseEntity<User>(userRepository.saveUser(user), HttpStatus.OK);
        } catch (UserAlreadyExistsException e) {
            log.info("UserController: saveUser - PRECONDITION_FAILED ");
            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        }
    }

    @PreAuthorize("hasAnyAuthority('" + CheckRole.ROLE_USER + "','" + CheckRole.ROLE_ADMIN + "')")
    @PutMapping(path = "/byemail")
    public ResponseEntity<User> updateUser(@RequestBody User user, @RequestParam("email") String email, Authentication authentication) {
        log.info("UserController: updateUser - email " + email);
        if ((user.getId() != null) && user.getId() != 0) {
            if(user.getRole().equalsIgnoreCase(CheckRole.ROLE_INIT)){
                log.info("UserController: updateUser - FORBIDDEN ");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            try {
                boolean hasRoleAdmin = CheckRole.hasRoleAdmin(authentication);
                log.info("UserController: updateUser - hasRoleAdmin " + hasRoleAdmin);
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                log.info("UserController: updateUser - userDetails.getUsername() " + userDetails.getUsername());
                if (hasRoleAdmin || userDetails.getUsername().equals(email)) {
                    if (!hasRoleAdmin) {
                        user.setRole(CheckRole.ROLE_USER);
                    }
                    log.info("UserController: updateUser - OK ");
                    return new ResponseEntity<User>(userRepository.updateUser(user, email), HttpStatus.OK);
                } else {
                    log.info("UserController: updateUser - FORBIDDEN ");
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (UserNotFoundException e) {
                log.info("UserController: updateUser - NOT_FOUND ");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } catch (UserAlreadyExistsException e) {
                log.info("UserController: updateUser - PRECONDITION_FAILED ");
                return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
            }
        } else {
            log.info("UserController: updateUser - BAD_REQUEST ");
            return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/byemail")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email, Authentication authentication) {
        log.info("UserController: getUserByEmail - email " + email );
        boolean hasRoleAdmin = CheckRole.hasRoleAdmin(authentication);
        log.info("UserController: getUserByEmail - hasRoleAdmin " + hasRoleAdmin );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("UserController: getUserByEmail - userDetails.getUsername() " + userDetails.getUsername());
        if (hasRoleAdmin || userDetails.getUsername().equals(email)) {
            Optional<User> optUser = userRepository.getByEmail(email);
            if (optUser.isPresent()) {
                log.info("UserController: getUserByEmail - OK ");
                return new ResponseEntity<User>(optUser.get(), HttpStatus.OK);
            } else {
                log.info("UserController: getUserByEmail - NOT_FOUND ");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            log.info("UserController: getUserByEmail - FORBIDDEN ");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping(path = "/byemail")
    public ResponseEntity<User> deleteUser(@RequestParam("email") String email, Authentication authentication) {
        log.info("UserController: deleteUser - email " + email);
        try {
            boolean hasRoleAdmin = CheckRole.hasRoleAdmin(authentication);
            log.info("UserController: deleteUser - hasRoleAdmin " + hasRoleAdmin );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.info("UserController: deleteUser - userDetails.getUsername() " + userDetails.getUsername());
            if (hasRoleAdmin || userDetails.getUsername().equals(email)) {
                log.info("UserController: deleteUser - OK ");
                return new ResponseEntity<User>(userRepository.deleteUser(email), HttpStatus.OK);
            } else {
                log.info("UserController: deleteUser - FORBIDDEN ");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (UserNotFoundException e) {
            log.info("UserController: deleteUser - NOT_FOUND ");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}