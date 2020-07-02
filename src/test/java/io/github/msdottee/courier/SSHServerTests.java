package io.github.msdottee.courier;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.SshException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes={CourierApplication.class})
public class SSHServerTests {

    @Test
    public void testThrowsAuthenticationErrorWithInvalidCredentials() {
        Exception exception = assertThrows(SshException.class, () -> {
            try(SshClient client = SshClient.setUpDefaultClient()) {
                client.start();

                try (ClientSession session = client.connect("test", "localhost", 2200)
                        .verify(1000)
                        .getSession()) {
                    session.auth().verify(1000);
                }
            }
        });

        String expectedMessage = "No more authentication methods available";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
