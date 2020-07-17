package io.github.msdottee.courier.service;

import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystem;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class S3SFTPSubsystemFactory extends SftpSubsystemFactory {

    @Autowired
    private S3FileSystemFactory s3FileSystemFactory;

    @Override
    public Command createSubsystem(ChannelSession channel) throws IOException {
        SftpSubsystem subsystem = new SftpSubsystem(
                resolveExecutorService(),
                getUnsupportedAttributePolicy(), getFileSystemAccessor(),
                getErrorStatusDataHandler());
        subsystem.setFileSystem(s3FileSystemFactory.createFileSystem(channel.getSession()));
        GenericUtils.forEach(getRegisteredListeners(), subsystem::addSftpEventListener);
        return subsystem;
    }
}
