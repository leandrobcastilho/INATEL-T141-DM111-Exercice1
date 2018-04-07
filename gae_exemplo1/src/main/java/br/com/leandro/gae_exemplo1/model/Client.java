package br.com.leandro.gae_exemplo1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.ClientDetails;

import java.io.Serializable;
import java.util.*;

public class Client implements Serializable, ClientDetails {

//    private String clientId;
//
//    private Set<String> resourceIds;
//
//    private boolean secretRequired;
//
//    private String clientSecret;
//
//    private boolean scoped;
//
//    private Set<String> scope;
//
//    private Set<String> getAuthorizedGrantTypes;
//
//    private Set<String> getRegisteredRedirectUri;
//
//    private Collection<GrantedAuthority> getAuthorities;
//
//    private Integer getAccessTokenValiditySeconds;
//
//    private Integer getRefreshTokenValiditySeconds;
//
//    private boolean isAutoApprove(String var1);
//
//    private Map<String, Object> getAdditionalInformation();


    @Override
    public String getClientId() {
        return null;
    }

    @Override
    public Set<String> getResourceIds() {
        return null;
    }

    @Override
    public boolean isSecretRequired() {
        return false;
    }

    @Override
    public String getClientSecret() {
        return null;
    }

    @Override
    public boolean isScoped() {
        return false;
    }

    @Override
    public Set<String> getScope() {
        return null;
    }

    @Override
    public Set<String> getAuthorizedGrantTypes() {
        return null;
    }

    @Override
    public Set<String> getRegisteredRedirectUri() {
        return null;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public Integer getAccessTokenValiditySeconds() {
        return null;
    }

    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return null;
    }

    @Override
    public boolean isAutoApprove(String s) {
        return false;
    }

    @Override
    public Map<String, Object> getAdditionalInformation() {
        return null;
    }
}