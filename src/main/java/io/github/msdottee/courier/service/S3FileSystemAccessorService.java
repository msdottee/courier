package io.github.msdottee.courier.service;

import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.FileLock;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class S3FileSystemAccessorService implements SftpFileSystemAccessor {
    /**
     * Invoked in order to resolve remote file paths reference by the client into ones accessible by the server
     *
     * @param session    The {@link ServerSession} through which the request was received
     * @param subsystem  The SFTP subsystem instance that manages the session
     * @param rootDir    The default root directory used to resolve relative paths - a.k.a. the
     *                   {@code chroot} location
     * @param remotePath The remote path - separated by '/'
     * @return The local {@link Path}
     * @throws IOException          If failed to resolve the local path
     * @throws InvalidPathException If bad local path specification
     * @see SftpSubsystemEnvironment#getDefaultDirectory()
     * SftpSubsystemEnvironment#getDefaultDirectory()
     */
    @Override
    public Path resolveLocalFilePath(ServerSession session, SftpSubsystemProxy subsystem, Path rootDir, String remotePath) throws IOException, InvalidPathException {
        throw new UnsupportedOperationException();
    }

    /**
     * Called whenever a new file is opened
     *
     * @param session    The {@link ServerSession} through which the request was received
     * @param subsystem  The SFTP subsystem instance that manages the session
     * @param fileHandle The {@link FileHandle} representing the created channel - may be {@code null} if not invoked
     *                   within the context of such a handle (special cases)
     * @param file       The requested <U>local</U> file {@link Path} - same one returned by
     *                   {@link #resolveLocalFilePath(ServerSession, SftpSubsystemProxy, Path, String)
     *                   resolveLocalFilePath}
     * @param handle     The assigned file handle through which the remote peer references this file. May be
     *                   {@code null}/empty if the request is due to some internal functionality instead of due to
     *                   peer requesting a handle to a file.
     * @param options    The requested {@link OpenOption}s
     * @param attrs      The requested {@link FileAttribute}s
     * @return The opened {@link SeekableByteChannel}
     * @throws IOException If failed to open
     */
    @Override
    public SeekableByteChannel openFile(ServerSession session, SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Called when locking a section of a file is requested
     *
     * @param session    The {@link ServerSession} through which the request was received
     * @param subsystem  The SFTP subsystem instance that manages the session
     * @param fileHandle The {@link FileHandle} representing the created channel
     * @param file       The requested <U>local</U> file {@link Path} - same one returned by
     *                   {@link #resolveLocalFilePath(ServerSession, SftpSubsystemProxy, Path, String)
     *                   resolveLocalFilePath}
     * @param handle     The assigned file handle through which the remote peer references this file
     * @param channel    The original {@link Channel} that was returned by
     *                   {@link #openFile(ServerSession, SftpSubsystemProxy, FileHandle, Path, String, Set, FileAttribute...)}
     * @param position   The position at which the locked region is to start - must be non-negative
     * @param size       The size of the locked region; must be non-negative, and the sum
     *                   <tt>position</tt>&nbsp;+&nbsp;<tt>size</tt> must be non-negative
     * @param shared     {@code true} to request a shared lock, {@code false} to request an exclusive lock
     * @return A lock object representing the newly-acquired lock, or {@code null} if the lock could not be
     * acquired because another program holds an overlapping lock
     * @throws IOException If failed to honor the request
     * @see FileChannel#tryLock(long, long, boolean)
     */
    @Override
    public FileLock tryLock(ServerSession session, SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle, Channel channel, long position, long size, boolean shared) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Called when file meta-data re-synchronization is required
     *
     * @param session    The {@link ServerSession} through which the request was received
     * @param subsystem  The SFTP subsystem instance that manages the session
     * @param fileHandle The {@link FileHandle} representing the created channel
     * @param file       The requested <U>local</U> file {@link Path} - same one returned by
     *                   {@link #resolveLocalFilePath(ServerSession, SftpSubsystemProxy, Path, String)
     *                   resolveLocalFilePath}
     * @param handle     The assigned file handle through which the remote peer references this file
     * @param channel    The original {@link Channel} that was returned by
     *                   {@link #openFile(ServerSession, SftpSubsystemProxy, FileHandle, Path, String, Set, FileAttribute...)}
     * @throws IOException If failed to execute the request
     * @see FileChannel#force(boolean)
     * @see <A HREF="https://github.com/openssh/openssh-portable/blob/master/PROTOCOL">OpenSSH - section
     * 10</A>
     */
    @Override
    public void syncFileData(ServerSession session, SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle, Channel channel) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Called to inform the accessor that it should close the file
     *
     * @param session    The {@link ServerSession} through which the request was received
     * @param subsystem  The SFTP subsystem instance that manages the session
     * @param fileHandle The {@link FileHandle} representing the created channel - may be {@code null} if not invoked
     *                   within the context of such a handle (special cases)
     * @param file       The requested <U>local</U> file {@link Path} - same one returned by
     *                   {@link #resolveLocalFilePath(ServerSession, SftpSubsystemProxy, Path, String)
     *                   resolveLocalFilePath}
     * @param handle     The assigned file handle through which the remote peer references this file
     * @param channel    The original {@link Channel} that was returned by
     *                   {@link #openFile(ServerSession, SftpSubsystemProxy, FileHandle, Path, String, Set, FileAttribute...)}
     * @param options    The original options used to open the channel
     * @throws IOException If failed to execute the request
     */
    @Override
    public void closeFile(ServerSession session, SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle, Channel channel, Set<? extends OpenOption> options) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Called when a new directory stream is requested
     *
     * @param session   The {@link ServerSession} through which the request was received
     * @param subsystem The SFTP subsystem instance that manages the session
     * @param dirHandle The {@link DirectoryHandle} representing the stream
     * @param dir       The requested <U>local</U> directory {@link Path} - same one returned by
     *                  {@link #resolveLocalFilePath(ServerSession, SftpSubsystemProxy, Path, String)
     *                  resolveLocalFilePath}
     * @param handle    The assigned directory handle through which the remote peer references this directory
     * @return The opened {@link DirectoryStream}
     * @throws IOException If failed to open
     */
    @Override
    public DirectoryStream<Path> openDirectory(ServerSession session, SftpSubsystemProxy subsystem, DirectoryHandle dirHandle, Path dir, String handle) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Called when a directory stream is no longer required
     *
     * @param session   The {@link ServerSession} through which the request was received
     * @param subsystem The SFTP subsystem instance that manages the session
     * @param dirHandle The {@link DirectoryHandle} representing the stream - may be {@code null} if not invoked
     *                  within the context of such a handle (special cases)
     * @param dir       The requested <U>local</U> directory {@link Path} - same one returned by
     *                  {@link #resolveLocalFilePath(ServerSession, SftpSubsystemProxy, Path, String)
     *                  resolveLocalFilePath}
     * @param handle    The assigned directory handle through which the remote peer references this directory
     * @param ds        The disposed {@link DirectoryStream}
     * @throws IOException If failed to open
     */
    @Override
    public void closeDirectory(ServerSession session, SftpSubsystemProxy subsystem, DirectoryHandle dirHandle, Path dir, String handle, DirectoryStream<Path> ds) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Invoked when required to retrieve file attributes for a specific file system view
     *
     * @param session   The {@link ServerSession} through which the request was received
     * @param subsystem The SFTP subsystem instance that manages the session
     * @param file      The requested <U>local</U> file {@link Path} - same one returned by
     *                  {@link #resolveLocalFilePath(ServerSession, SftpSubsystemProxy, Path, String)
     *                  resolveLocalFilePath}
     * @param view      The required view name
     * @param options   The access {@link LinkOption}-s
     * @return A {@link Map} of all the attributes available for the file in the view
     * @throws IOException If failed to read the attributes
     * @see Files#readAttributes(Path, String, LinkOption...)
     */
    @Override
    public Map<String, ?> readFileAttributes(ServerSession session, SftpSubsystemProxy subsystem, Path file, String view, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets a view attribute for a local file
     *
     * @param session   The {@link ServerSession} through which the request was received
     * @param subsystem The SFTP subsystem instance that manages the session
     * @param file      The requested <U>local</U> file {@link Path} - same one returned by
     *                  {@link #resolveLocalFilePath(ServerSession, SftpSubsystemProxy, Path, String)
     *                  resolveLocalFilePath}
     * @param view      The required view name
     * @param attribute The attribute name
     * @param value     The attribute value
     * @param options   The access {@link LinkOption}-s
     * @throws IOException If failed to set the attribute
     */
    @Override
    public void setFileAttribute(ServerSession session, SftpSubsystemProxy subsystem, Path file, String view, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserPrincipal resolveFileOwner(ServerSession session, SftpSubsystemProxy subsystem, Path file, UserPrincipal name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFileOwner(ServerSession session, SftpSubsystemProxy subsystem, Path file, Principal value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public GroupPrincipal resolveGroupOwner(ServerSession session, SftpSubsystemProxy subsystem, Path file, GroupPrincipal name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGroupOwner(ServerSession session, SftpSubsystemProxy subsystem, Path file, Principal value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFilePermissions(ServerSession session, SftpSubsystemProxy subsystem, Path file, Set<PosixFilePermission> perms, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFileAccessControl(ServerSession session, SftpSubsystemProxy subsystem, Path file, List<AclEntry> acl, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createDirectory(ServerSession session, SftpSubsystemProxy subsystem, Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Invoked in order to create a link to a path
     *
     * @param session   The {@link ServerSession} through which the request was received
     * @param subsystem The SFTP subsystem instance that manages the session
     * @param link      The requested <U>link</U> {@link Path} - same one returned by
     *                  {@link #resolveLocalFilePath(ServerSession, SftpSubsystemProxy, Path, String)
     *                  resolveLocalFilePath}
     * @param existing  The <U>existing</U> {@link Path} that the link should reference
     * @param symLink   {@code true} if this should be a symbolic link
     * @throws IOException If failed to create the link
     * @see Files#createLink(Path, Path)
     * @see Files#createSymbolicLink(Path, Path, FileAttribute...)
     */
    @Override
    public void createLink(ServerSession session, SftpSubsystemProxy subsystem, Path link, Path existing, boolean symLink) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String resolveLinkTarget(ServerSession session, SftpSubsystemProxy subsystem, Path link) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void renameFile(ServerSession session, SftpSubsystemProxy subsystem, Path oldPath, Path newPath, Collection<CopyOption> opts) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyFile(ServerSession session, SftpSubsystemProxy subsystem, Path src, Path dst, Collection<CopyOption> opts) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeFile(ServerSession session, SftpSubsystemProxy subsystem, Path path, boolean isDirectory) throws IOException {
        throw new UnsupportedOperationException();
    }
}
