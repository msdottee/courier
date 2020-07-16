package io.github.msdottee.courier.service;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.SubsystemFactory;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;

@Service
public class SSHServerService {

    @Value("${sshd.port:2200}")
    private int port;

    @Autowired
    private PublicKeyAuthenticatorService publicKeyAuthenticatorService;

    @Autowired
    private S3FileSystemFactory s3FileSystemFactory;

    @PostConstruct
    public void initializeSshServer() throws IOException {
        SshServer sshd = SshServer.setUpDefaultServer();

        sshd.setPort(port);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshd.setPublickeyAuthenticator(publicKeyAuthenticatorService);
        sshd.setFileSystemFactory(s3FileSystemFactory);
        sshd.setSubsystemFactories(Collections.singletonList(createFileSystemFactory()));

        sshd.start();
    }

    private SubsystemFactory createFileSystemFactory() {
        return new SftpSubsystemFactory.Builder().build();
    }
}
