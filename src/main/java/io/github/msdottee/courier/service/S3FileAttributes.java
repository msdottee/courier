package io.github.msdottee.courier.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import org.apache.sshd.client.subsystem.sftp.fs.SftpFileSystem;

import java.nio.file.Files;
import java.nio.file.attribute.*;
import java.util.HashSet;
import java.util.Set;

public class S3FileAttributes implements BasicFileAttributes, PosixFileAttributes {

    private final ObjectMetadata objectMetadata;
    private final String path;

    public S3FileAttributes(ObjectMetadata objectMetadata, String path) {
        this.objectMetadata = objectMetadata;
        this.path = path;
    }

    /**
     * Returns the time of last modification.
     *
     * <p> If the file system implementation does not support a time stamp
     * to indicate the time of last modification then this method returns an
     * implementation specific default value, typically a {@code FileTime}
     * representing the epoch (1970-01-01T00:00:00Z).
     *
     * @return a {@code FileTime} representing the time the file was last
     * modified
     */
    @Override
    public FileTime lastModifiedTime() {
        if (objectMetadata.getLastModified() == null) {
            return FileTime.fromMillis(0L);
        }

        return FileTime.fromMillis(objectMetadata.getLastModified().getTime());
    }

    /**
     * Returns the time of last access.
     *
     * <p> If the file system implementation does not support a time stamp
     * to indicate the time of last access then this method returns
     * an implementation specific default value, typically the {@link
     * #lastModifiedTime() last-modified-time} or a {@code FileTime}
     * representing the epoch (1970-01-01T00:00:00Z).
     *
     * @return a {@code FileTime} representing the time of last access
     */
    @Override
    public FileTime lastAccessTime() {
        return FileTime.fromMillis(0L);
    }

    /**
     * Returns the creation time. The creation time is the time that the file
     * was created.
     *
     * <p> If the file system implementation does not support a time stamp
     * to indicate the time when the file was created then this method returns
     * an implementation specific default value, typically the {@link
     * #lastModifiedTime() last-modified-time} or a {@code FileTime}
     * representing the epoch (1970-01-01T00:00:00Z).
     *
     * @return a {@code FileTime} representing the time the file was created
     */
    @Override
    public FileTime creationTime() {
        return lastModifiedTime();
    }

    /**
     * Tells whether the file is a regular file with opaque content.
     *
     * @return {@code true} if the file is a regular file with opaque content
     */
    @Override
    public boolean isRegularFile() {
        return !isDirectory();
    }

    /**
     * Tells whether the file is a directory.
     *
     * @return {@code true} if the file is a directory
     */
    @Override
    public boolean isDirectory() {
        return objectMetadata.getLastModified() == null;
    }

    /**
     * Tells whether the file is a symbolic link.
     *
     * @return {@code true} if the file is a symbolic link
     */
    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    /**
     * Tells whether the file is something other than a regular file, directory,
     * or symbolic link.
     *
     * @return {@code true} if the file something other than a regular file,
     * directory or symbolic link
     */
    @Override
    public boolean isOther() {
        return false;
    }

    /**
     * Returns the size of the file (in bytes). The size may differ from the
     * actual size on the file system due to compression, support for sparse
     * files, or other reasons. The size of files that are not {@link
     * #isRegularFile regular} files is implementation specific and
     * therefore unspecified.
     *
     * @return the file size, in bytes
     */
    @Override
    public long size() {
        return objectMetadata.getContentLength();
    }

    /**
     * Returns an object that uniquely identifies the given file, or {@code
     * null} if a file key is not available. On some platforms or file systems
     * it is possible to use an identifier, or a combination of identifiers to
     * uniquely identify a file. Such identifiers are important for operations
     * such as file tree traversal in file systems that support <a
     * href="../package-summary.html#links">symbolic links</a> or file systems
     * that allow a file to be an entry in more than one directory. On UNIX file
     * systems, for example, the <em>device ID</em> and <em>inode</em> are
     * commonly used for such purposes.
     *
     * <p> The file key returned by this method can only be guaranteed to be
     * unique if the file system and files remain static. Whether a file system
     * re-uses identifiers after a file is deleted is implementation dependent and
     * therefore unspecified.
     *
     * <p> File keys returned by this method can be compared for equality and are
     * suitable for use in collections. If the file system and files remain static,
     * and two files are the {@link Files#isSameFile same} with
     * non-{@code null} file keys, then their file keys are equal.
     *
     * @return an object that uniquely identifies the given file, or {@code null}
     * @see Files#walkFileTree
     */
    @Override
    public Object fileKey() {
        return path;
    }

    /**
     * Returns the owner of the file.
     *
     * @return the file owner
     * @see PosixFileAttributeView#setOwner
     */
    @Override
    public UserPrincipal owner() {
        return new SftpFileSystem.DefaultGroupPrincipal("nobody");
    }

    /**
     * Returns the group owner of the file.
     *
     * @return the file group owner
     * @see PosixFileAttributeView#setGroup
     */
    @Override
    public GroupPrincipal group() {
        return new SftpFileSystem.DefaultGroupPrincipal("nobody");
    }

    /**
     * Returns the permissions of the file. The file permissions are returned
     * as a set of {@link PosixFilePermission} elements. The returned set is a
     * copy of the file permissions and is modifiable. This allows the result
     * to be modified and passed to the {@link PosixFileAttributeView#setPermissions
     * setPermissions} method to update the file's permissions.
     *
     * @return the file permissions
     * @see PosixFileAttributeView#setPermissions
     */
    @Override
    public Set<PosixFilePermission> permissions() {
        return new HashSet<>() {{
            add(PosixFilePermission.OWNER_READ);
            add(PosixFilePermission.OWNER_WRITE);

            add(PosixFilePermission.GROUP_READ);
            add(PosixFilePermission.GROUP_WRITE);

            add(PosixFilePermission.OTHERS_READ);
            add(PosixFilePermission.OTHERS_WRITE);
        }};
    }
}
