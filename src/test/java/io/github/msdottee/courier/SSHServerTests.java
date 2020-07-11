package io.github.msdottee.courier;

import io.github.msdottee.courier.entity.SshPublicKey;
import io.github.msdottee.courier.entity.User;
import io.github.msdottee.courier.repository.SshPublicKeyRepository;
import io.github.msdottee.courier.repository.UserRepository;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SSHServerTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SshPublicKeyRepository sshPublicKeyRepository;

    @BeforeEach
    public void before() throws IOException {
        User user = userRepository.findByUserName("test");

        if (user != null) {
            List<SshPublicKey> sshPublicKeyList = sshPublicKeyRepository.findByUser(user);
            sshPublicKeyRepository.deleteAll(sshPublicKeyList);
            userRepository.delete(user);
        }

        User newUser = new User();
        newUser.setUserName("test");

        userRepository.save(newUser);

        SshPublicKey newSshPublicKey = new SshPublicKey();
        newSshPublicKey.setSshKey(Files.readString(
                Paths.get("src/test/resources/keypairs/test/id_rsa.pub"), StandardCharsets.UTF_8));
        newSshPublicKey.setUser(newUser);

        sshPublicKeyRepository.save(newSshPublicKey);
    }

    @Test
    public void testIsSuccessfulWithUserNameAndKeyFromDatabase() throws IOException, GeneralSecurityException {
        createClientSession("test", loadKeypair("src/test/resources/keypairs/test/id_rsa"));
    }

    @Test
    public void testThrowsAuthenticationErrorWithInvalidUser() {
        Exception exception = assertThrows(SshException.class, () ->
                createClientSession("invalid"));

        String expectedMessage = "No more authentication methods available";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testFailsWhenUserSuppliesUnmatchedKey() throws IOException, GeneralSecurityException {
        Exception exception = assertThrows(SshException.class, () ->
        createClientSession("test", loadKeypair("src/test/resources/keypairs/unmatched/id_rsa")));

        String expectedMessage = "No more authentication methods available";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    private void createClientSession(String username) throws IOException {
        createClientSession(username, null);
    }

    private void createClientSession(String username, KeyPair keyPair) throws IOException {
        try(SshClient client = SshClient.setUpDefaultClient()) {

            if (null != keyPair) {
                client.addPublicKeyIdentity(keyPair);
            }

            client.start();

            try (ClientSession session = client.connect(username, "localhost", 2200)
                    .verify(1000)
                    .getSession()) {
                session.auth().verify(1000);
            }
        }
    }

    private KeyPair loadKeypair(String privateKeyPath) throws IOException, GeneralSecurityException {
        try (InputStream privateKeyStream = new FileInputStream(privateKeyPath)) {
            Iterable<KeyPair> keyPairIterable =
                    SecurityUtils.loadKeyPairIdentities(null, null, privateKeyStream, null);
            KeyPair keyPair = keyPairIterable.iterator().next();

            if (null == keyPair) {
                throw new FileNotFoundException(privateKeyPath);
            }

            return keyPair;
        }
    }
}