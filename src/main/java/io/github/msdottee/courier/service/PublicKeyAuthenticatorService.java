package io.github.msdottee.courier.service;

import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.stereotype.Service;

import java.security.PublicKey;

@Service
public class PublicKeyAuthenticatorService implements PublickeyAuthenticator {

    /**
     * Check the validity of a public key.
     *
     * @param username the username
     * @param key      the key
     * @param session  the server session
     * @return a boolean indicating if authentication succeeded or not
     * @throws AsyncAuthException If the authentication is performed asynchronously
     */
    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) throws AsyncAuthException {
        return false;
    }
}
