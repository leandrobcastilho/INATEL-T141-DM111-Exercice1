package br.com.leandro.gae_exemplo1.repository;

import br.com.leandro.gae_exemplo1.exception.UserAlreadyExistsException;
import br.com.leandro.gae_exemplo1.exception.UserNotFoundException;
import br.com.leandro.gae_exemplo1.model.User;
import br.com.leandro.gae_exemplo1.util.CheckRole;
import com.google.appengine.api.datastore.*;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import java.util.*;
import java.util.logging.Logger;

@Repository
public class UserRepository {

    private static final Logger log = Logger.getLogger("UserRepository");

    private static final String USER_KIND = "Users";
    private static final String USER_KEY = "userKey";

    private static final String PROPERTY_ID = "UserId";
    private static final String PROPERTY_EMAIL = "email";
    private static final String PROPERTY_PASSWORD = "password";
    private static final String PROPERTY_GCM_REG_ID = "gcmRegId";
    private static final String PROPERTY_LAST_LOGIN = "lastLogin";
    private static final String PROPERTY_LAST_GCM_REGISTER = "lastGCMRegister";
    private static final String PROPERTY_ROLE = "role";
    private static final String PROPERTY_ENABLED = "enabled";
    private static final String PROPERTY_OWNER_EMAIL = "ownerEmail";


    @PostConstruct
    public void init() {

        String email = "leandrobcastilho@hotmail.com";
        String password = "borges";
        User adminUser;
        Optional<User> optAdminUser = this.getByEmail(email);
        try {
            if (optAdminUser.isPresent()) {
                adminUser = optAdminUser.get();
                if (!adminUser.getRole().equals(CheckRole.ROLE_INIT)) {
                    adminUser.setRole(CheckRole.ROLE_INIT);
                    this.updateUser(adminUser, email);
                }
            } else {
                adminUser = new User();
                adminUser.setRole(CheckRole.ROLE_INIT);
                adminUser.setEnabled(true);
                adminUser.setPassword(password);
                adminUser.setEmail(email);
                adminUser.setOwnerEmail("");
                this.saveUser(adminUser);
            }
        } catch (UserAlreadyExistsException | UserNotFoundException e) {
            log.severe("Falha ao criar usuário INIT");
        }
    }


    private Optional<Entity> getEntityByEmail(DatastoreService dataStore, String email) {

        log.info("UserRepository: getEntityByEmail - email " + email);
        Query.Filter filter = new Query.FilterPredicate(PROPERTY_EMAIL, Query.FilterOperator.EQUAL, email);
        Query query = new Query(USER_KIND).setFilter(filter);
        Entity userEntity = dataStore.prepare(query).asSingleEntity();
        log.info("UserRepository: getEntityByEmail - userEntity " + userEntity);
        if (userEntity != null) {
            return Optional.ofNullable(userEntity);
        } else {
            return Optional.empty();
        }
    }

    public Optional<User> getByEmail(String email) {

        log.info("UserRepository: getByEmail - email " + email);
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        Entity userEntity;
        Optional<Entity> optUserEntity = this.getEntityByEmail(dataStore, email);
        boolean isPresent = optUserEntity.isPresent();
        log.info("UserRepository: getByEmail - isPresent " + isPresent);
        if (isPresent) {
            userEntity = optUserEntity.get();
            return Optional.ofNullable(entityToUser(userEntity));
        } else {
            return Optional.empty();
        }
    }

    public Optional<User> getByOwnerEmail(String ownerEmail) {

        log.info("UserRepository: getByOwnerEmail - ownerEmail " + ownerEmail);
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        Query.Filter filter = new Query.FilterPredicate(PROPERTY_OWNER_EMAIL, Query.FilterOperator.EQUAL, ownerEmail);
        Query query = new Query(USER_KIND).setFilter(filter);
        Entity userEntity = dataStore.prepare(query).asSingleEntity();
        log.info("UserRepository: getByOwnerEmail - userEntity " + userEntity);
        if (userEntity != null) {
            return Optional.ofNullable(entityToUser(userEntity));
        } else {
            return Optional.empty();
        }

    }


    public User saveUser(User user) throws UserAlreadyExistsException {

        log.info("UserRepository: saveUser ");
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        boolean isEmailExist = checkIfEmailExist(user, dataStore);
        log.info("UserRepository: saveUser - isEmailExist "+ isEmailExist);
        if (!isEmailExist) {
            Key userKey = KeyFactory.createKey(USER_KIND, USER_KEY);
            Entity userEntity = new Entity(USER_KIND, userKey);
            userToEntity(user, userEntity);
            dataStore.put(userEntity);
            user.setId(userEntity.getKey().getId());
            log.info("UserRepository: saveUser = ok");
            return user;
        } else {
            log.info("UserRepository: saveUser = AlreadyExists");
            throw new UserAlreadyExistsException("Usuário " + user.getEmail() + " já existe");
        }
    }

    public User updateUser(User user, String email) throws UserNotFoundException, UserAlreadyExistsException {

        log.info("UserRepository: updateUser ");
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        boolean isEmailExist = checkIfEmailExist(user, dataStore);
        log.info("UserRepository: updateUser -  isEmailExist "+ isEmailExist);
        if (!isEmailExist) {
            Entity userEntity;
            Optional<Entity> optUserEntity = this.getEntityByEmail(dataStore, email);
            if (optUserEntity.isPresent()) {
                userEntity = optUserEntity.get();
                userToEntity(user, userEntity);
                dataStore.put(userEntity);
                user.setId(userEntity.getKey().getId());
                log.info("UserRepository: updateUser = ok");
                return user;
            } else {
                log.info("UserRepository: updateUser = NotFound");
                throw new UserNotFoundException("Usuário " + email + " não encontrado");
            }
        } else {
            log.info("UserRepository: updateUser AlreadyExists");
            throw new UserAlreadyExistsException("Usuário " + user.getEmail() + " já existe");
        }
    }

    public List<User> getUsers() {

        log.info("UserRepository: getUsers ");
        List<User> users = new ArrayList<>();
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        Query query;
        query = new Query(USER_KIND).addSort(PROPERTY_EMAIL, Query.SortDirection.ASCENDING);
        List<Entity> userEntities = dataStore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        for (Entity userEntity : userEntities) {
            User user = entityToUser(userEntity);
            users.add(user);
        }
        return users;
    }

    public List<User> getUsersCreateByEmail(String email) {

        log.info("UserRepository: getUsersCreateByEmail - email " + email);
        List<User> users = new ArrayList<>();
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        Query.Filter filter = new Query.FilterPredicate(PROPERTY_OWNER_EMAIL, Query.FilterOperator.EQUAL, email);
        Query query;
        query = new Query(USER_KIND).setFilter(filter).addSort(PROPERTY_EMAIL, Query.SortDirection.ASCENDING);
        List<Entity> userEntities = dataStore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        for (Entity userEntity : userEntities) {
            User user = entityToUser(userEntity);
            users.add(user);
        }
        return users;
    }

    public User deleteUser(String email) throws UserNotFoundException {

        log.info("UserRepository: deleteUser ");
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        Entity userEntity;
        Optional<Entity> optUserEntity = this.getEntityByEmail(dataStore, email);
        if (optUserEntity.isPresent()) {
            userEntity = optUserEntity.get();
            dataStore.delete(userEntity.getKey());
            log.info("UserRepository: deleteUser = ok");
            return entityToUser(userEntity);
        } else {
            log.info("UserRepository: deleteUser = NotFound");
            throw new UserNotFoundException("Usuário " + email + " não encontrado");
        }
    }


    private boolean checkIfUserIsOwner(User user, Entity userEntity) {

        log.info("UserRepository: checkIfUserIsOwner ");
        if (user.getId() == null) {
            log.info("UserRepository: checkIfUserIsOwner = TRUE ");
            return true;
        } else {
            boolean sameID = userEntity.getKey().getId() != user.getId();
            if( sameID ){
                log.info("UserRepository: checkIfUserIsOwner = TRUE ");
                return true;
            }else{
                log.info("UserRepository: checkIfUserIsOwner = FALSE ");
                return false;
            }
        }
    }

    private boolean checkIfEmailExist(User user, DatastoreService dataStore) {

        log.info("UserRepository: checkIfEmailExist ");
        Entity userEntity;
        Optional<Entity> optUserEntity = this.getEntityByEmail(dataStore, user.getEmail());
        boolean isPresent = optUserEntity.isPresent();
        if (isPresent) {
            log.info("UserRepository: checkIfEmailExist - isPresent " + isPresent);
            userEntity = optUserEntity.get();
            return checkIfUserIsOwner(user, userEntity);
        } else {
            log.info("UserRepository: checkIfEmailExist = FALSE ");
            return false;
        }
    }


    public void updateUserLogin(User user) {
        boolean canUseCache = true;

        Cache cache;
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());

            boolean saveOnCache = true;
            if (cache.containsKey(user.getEmail())) {
                Date lastLogin = (Date) cache.get(user.getEmail());
                if ((Calendar.getInstance().getTime().getTime() - lastLogin.getTime()) < 30000) {
                    saveOnCache = false;
                }
            }

            if (saveOnCache) {
                cache.put(user.getEmail(), (Date) Calendar.getInstance().getTime());
                canUseCache = false;
            }
        } catch (CacheException e) {
            canUseCache = false;
        }

        if (!canUseCache) {
            user.setLastLogin((Date) Calendar.getInstance().getTime());
            try {
                this.updateUser(user, user.getEmail());
            } catch (UserAlreadyExistsException | UserNotFoundException e ) {
                log.severe("Falha ao atualizar último login do usuário");
            }
        }
    }


    private void userToEntity(User user, Entity userEntity) {
        userEntity.setProperty(PROPERTY_ID, user.getId());
        userEntity.setProperty(PROPERTY_EMAIL, user.getEmail());
        userEntity.setProperty(PROPERTY_PASSWORD, user.getPassword());
        userEntity.setProperty(PROPERTY_GCM_REG_ID, user.getGcmRegId());
        userEntity.setProperty(PROPERTY_LAST_LOGIN, user.getLastLogin());
        userEntity.setProperty(PROPERTY_LAST_GCM_REGISTER, user.getLastGCMRegister());
        userEntity.setProperty(PROPERTY_ROLE, user.getRole());
        userEntity.setProperty(PROPERTY_ENABLED, user.isEnabled());
        userEntity.setProperty(PROPERTY_OWNER_EMAIL, user.getOwnerEmail());
    }

    private User entityToUser(Entity userEntity) {
        User user = new User();
        user.setId(userEntity.getKey().getId());
        user.setEmail((String) userEntity.getProperty(PROPERTY_EMAIL));
        user.setPassword((String) userEntity.getProperty(PROPERTY_PASSWORD));
        user.setGcmRegId((String) userEntity.getProperty(PROPERTY_GCM_REG_ID));
        user.setLastLogin((Date) userEntity.getProperty(PROPERTY_LAST_LOGIN));
        user.setLastGCMRegister((Date) userEntity.getProperty(PROPERTY_LAST_GCM_REGISTER));
        user.setRole((String) userEntity.getProperty(PROPERTY_ROLE));
        user.setEnabled((Boolean) userEntity.getProperty(PROPERTY_ENABLED));
        user.setOwnerEmail((String) userEntity.getProperty(PROPERTY_OWNER_EMAIL));
        return user;
    }
}
