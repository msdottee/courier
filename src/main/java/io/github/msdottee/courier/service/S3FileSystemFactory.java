package io.github.msdottee.courier.service;

import com.amazonaws.services.s3.AmazonS3;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.HashMap;

@Service
public class S3FileSystemFactory implements FileSystemFactory {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String awsS3Bucket;

    /**
     * @param session The session created for the user
     * @return The recommended user home directory - {@code null} if none
     * @throws IOException If failed to resolve user's home directory
     */
    @Override
    public Path getUserHomeDir(SessionContext session) throws IOException {
        return new S3Path();
    }

    /**
     * Create user specific file system.
     *
     * @param session The session created for the user
     * @return The current {@link FileSystem} for the provided session
     * @throws IOException if the file system can not be created
     */
    @Override
    public FileSystem createFileSystem(SessionContext session) throws IOException {
        return new S3FileSystemProvider(amazonS3).newFileSystem(URI.create("s3://" + awsS3Bucket), new HashMap<>());
    }
}
