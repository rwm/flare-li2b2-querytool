package de.li2b2.serverdemo;

import java.security.Principal;

import javax.inject.Singleton;
import javax.ws.rs.ext.Provider;

import de.sekmi.li2b2.services.token.AbstractTokenManager;

@Provider
public class TokenManagerImpl extends AbstractTokenManager<Principal>{
	@Override
	public Principal createPrincipal(String name) {
		return new Principal() {
			@Override
			public String getName() {
				return name;
			}
		};
	}
}
