package io.github.msdottee.courier.service;

import io.github.msdottee.courier.entity.SshPublicKey;
import io.github.msdottee.courier.entity.User;
import io.github.msdottee.courier.repository.SshPublicKeyRepository;
import io.github.msdottee.courier.repository.UserRepository;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.List;

@Service
@Transactional
public class PublicKeyAuthenticatorService implements PublickeyAuthenticator {

    private Logger LOGGER = LoggerFactory.getLogger(PublicKeyAuthenticatorService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SshPublicKeyRepository sshPublicKeyRepository;

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
    public boolean authenticate(String username, PublicKey key, ServerSession session) throws AsyncAuthException{
        User user = userRepository.findByUserName(username);

        if (null == user) {
            LOGGER.info("Failed to Login Missing User {}.", username);
            return false;
        }

        List<SshPublicKey> userSshPublicKeys = sshPublicKeyRepository.findByUser(user);

        for (SshPublicKey userSshPublicKey : userSshPublicKeys) {
            try {
                AuthorizedKeyEntry authorizedKeyEntry =
                        AuthorizedKeyEntry.parseAuthorizedKeyEntry(userSshPublicKey.getSshKey());

                PublicKey publicKey = authorizedKeyEntry.resolvePublicKey(null, PublicKeyEntryResolver.FAILING);

                if (publicKey.equals(key)) {
                    LOGGER.info("Successfully Logged in User {} with Public Key.", username);
                    return true;
                }
            } catch (IllegalArgumentException | IOException | GeneralSecurityException e) {
                LOGGER.error("Failed to Login User " + username +
                        " with Unparseable Public Key retrieved from the database.", e);
                return false;
            }
        }

        LOGGER.info("Failed to Login User {} with an Unrecognized Public Key.", username);
        return false;
    }
}
