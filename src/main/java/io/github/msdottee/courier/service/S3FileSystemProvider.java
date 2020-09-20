package io.github.msdottee.courier.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.stream.Stream;

public class S3FileSystemProvider extends FileSystemProvider {

    private final AmazonS3 s3Client;

    public S3FileSystemProvider(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Returns the URI scheme that identifies this provider.
     *
     * @return The URI scheme
     */
    @Override
    public String getScheme() {
        return "s3";
    }

    /**
     * Constructs a new {@code FileSystem} object identified by a URI. This
     * method is invoked by the {@link FileSystems#newFileSystem(URI, Map)}
     * method to open a new file system identified by a URI.
     *
     * <p> The {@code uri} parameter is an absolute, hierarchical URI, with a
     * scheme equal (without regard to case) to the scheme supported by this
     * provider. The exact form of the URI is highly provider dependent. The
     * {@code env} parameter is a map of provider specific properties to configure
     * the file system.
     *
     * <p> This method throws {@link FileSystemAlreadyExistsException} if the
     * file system already exists because it was previously created by an
     * invocation of this method. Once a file system is {@link
     * FileSystem#close closed} it is provider-dependent if the
     * provider allows a new file system to be created with the same URI as a
     * file system it previously created.
     *
     * @param uri URI reference
     * @param env A map of provider specific properties to configure the file system;
     *            may be empty
     * @return A new file system
     * @throws IllegalArgumentException         If the pre-conditions for the {@code uri} parameter aren't met,
     *                                          or the {@code env} parameter does not contain properties required
     *                                          by the provider, or a property value is invalid
     * @throws IOException                      An I/O error occurs creating the file system
     * @throws SecurityException                If a security manager is installed and it denies an unspecified
     *                                          permission required by the file system provider implementation
     * @throws FileSystemAlreadyExistsException If the file system has already been created
     */
    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        if (!getScheme().equals(uri.getScheme())) {
            throw new IllegalArgumentException("Invalid scheme provided. s3 is the only supported scheme.");
        }

        return new S3FileSystem(this, s3Client, uri.getHost());
    }

    /**
     * Returns an existing {@code FileSystem} created by this provider.
     *
     * <p> This method returns a reference to a {@code FileSystem} that was
     * created by invoking the {@link #newFileSystem(URI, Map) newFileSystem(URI,Map)}
     * method. File systems created the {@link #newFileSystem(Path, Map)
     * newFileSystem(Path,Map)} method are not returned by this method.
     * The file system is identified by its {@code URI}. Its exact form
     * is highly provider dependent. In the case of the default provider the URI's
     * path component is {@code "/"} and the authority, query and fragment components
     * are undefined (Undefined components are represented by {@code null}).
     *
     * <p> Once a file system created by this provider is {@link
     * FileSystem#close closed} it is provider-dependent if this
     * method returns a reference to the closed file system or throws {@link
     * FileSystemNotFoundException}. If the provider allows a new file system to
     * be created with the same URI as a file system it previously created then
     * this method throws the exception if invoked after the file system is
     * closed (and before a new instance is created by the {@link #newFileSystem
     * newFileSystem} method).
     *
     * <p> If a security manager is installed then a provider implementation
     * may require to check a permission before returning a reference to an
     * existing file system. In the case of the {@link FileSystems#getDefault
     * default} file system, no permission check is required.
     *
     * @param uri URI reference
     * @return The file system
     * @throws IllegalArgumentException    If the pre-conditions for the {@code uri} parameter aren't met
     * @throws FileSystemNotFoundException If the file system does not exist
     * @throws SecurityException           If a security manager is installed and it denies an unspecified
     *                                     permission.
     */
    @Override
    public FileSystem getFileSystem(URI uri) {
        return null;
    }

    /**
     * Return a {@code Path} object by converting the given {@link URI}. The
     * resulting {@code Path} is associated with a {@link FileSystem} that
     * already exists or is constructed automatically.
     *
     * <p> The exact form of the URI is file system provider dependent. In the
     * case of the default provider, the URI scheme is {@code "file"} and the
     * given URI has a non-empty path component, and undefined query, and
     * fragment components. The resulting {@code Path} is associated with the
     * default {@link FileSystems#getDefault default} {@code FileSystem}.
     *
     * <p> If a security manager is installed then a provider implementation
     * may require to check a permission. In the case of the {@link
     * FileSystems#getDefault default} file system, no permission check is
     * required.
     *
     * @param uri The URI to convert
     * @return The resulting {@code Path}
     * @throws IllegalArgumentException    If the URI scheme does not identify this provider or other
     *                                     preconditions on the uri parameter do not hold
     * @throws FileSystemNotFoundException The file system, identified by the URI, does not exist and
     *                                     cannot be created automatically
     * @throws SecurityException           If a security manager is installed and it denies an unspecified
     *                                     permission.
     */
    @Override
    public Path getPath(URI uri) {
        return null;
    }

    /**
     * Opens or creates a file, returning a seekable byte channel to access the
     * file. This method works in exactly the manner specified by the {@link
     * Files#newByteChannel(Path, Set, FileAttribute[])} method.
     *
     * @param path    the path to the file to open or create
     * @param options options specifying how the file is opened
     * @param attrs   an optional list of file attributes to set atomically when
     *                creating the file
     * @return a new seekable byte channel
     * @throws IllegalArgumentException      if the set contains an invalid combination of options
     * @throws UnsupportedOperationException if an unsupported open option is specified or the array contains
     *                                       attributes that cannot be set atomically when creating the file
     * @throws FileAlreadyExistsException    if a file of that name already exists and the {@link
     *                                       StandardOpenOption#CREATE_NEW CREATE_NEW} option is specified
     *                                       <i>(optional specific exception)</i>
     * @throws IOException                   if an I/O error occurs
     * @throws SecurityException             In the case of the default provider, and a security manager is
     *                                       installed, the {@link SecurityManager#checkRead(String) checkRead}
     *                                       method is invoked to check read access to the path if the file is
     *                                       opened for reading. The {@link SecurityManager#checkWrite(String)
     *                                       checkWrite} method is invoked to check write access to the path
     *                                       if the file is opened for writing. The {@link
     *                                       SecurityManager#checkDelete(String) checkDelete} method is
     *                                       invoked to check delete access if the file is opened with the
     *                                       {@code DELETE_ON_CLOSE} option.
     */
    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        return null;
    }

    /**
     * Opens a directory, returning a {@code DirectoryStream} to iterate over
     * the entries in the directory. This method works in exactly the manner
     * specified by the {@link
     * Files#newDirectoryStream(Path, DirectoryStream.Filter)}
     * method.
     *
     * @param dir    the path to the directory
     * @param filter the directory stream filter
     * @return a new and open {@code DirectoryStream} object
     * @throws NotDirectoryException if the file could not otherwise be opened because it is not
     *                               a directory <i>(optional specific exception)</i>
     * @throws IOException           if an I/O error occurs
     * @throws SecurityException     In the case of the default provider, and a security manager is
     *                               installed, the {@link SecurityManager#checkRead(String) checkRead}
     *                               method is invoked to check read access to the directory.
     */
    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        S3Path s3Dir = (S3Path) dir;

        S3FileSystem s3FileSystem = (S3FileSystem) dir.getFileSystem();
        
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
                .withBucketName(s3FileSystem.getBucket())
                .withPrefix(s3Dir.toS3Prefix())
                .withDelimiter(s3FileSystem.getSeparator());

        ListObjectsV2Result listObjectsV2Result = s3Client.listObjectsV2(listObjectsV2Request);

        Stream<Path> s3FilePaths = listObjectsV2Result
                .getObjectSummaries()
                .stream()
                .map(s3ObjectSummary -> (Path) new S3Path(s3FileSystem, dir.toString() + s3ObjectSummary.getKey()));

        Stream<Path> s3DirectoryPaths = listObjectsV2Result
                .getCommonPrefixes()
                .stream()
                .map(s3Prefix -> (Path) new S3Path(s3FileSystem, s3Prefix));

        Stream<Path> s3Paths = Stream.concat(s3DirectoryPaths, s3FilePaths);

        return new DirectoryStream<>() {
            @Override
            public Iterator<Path> iterator() {
                return s3Paths.iterator();
            }

            @Override
            public void close() throws IOException {

            }
        };
    }

    /**
     * Creates a new directory. This method works in exactly the manner
     * specified by the {@link Files#createDirectory} method.
     *
     * @param dir   the directory to create
     * @param attrs an optional list of file attributes to set atomically when
     *              creating the directory
     * @throws UnsupportedOperationException if the array contains an attribute that cannot be set atomically
     *                                       when creating the directory
     * @throws FileAlreadyExistsException    if a directory could not otherwise be created because a file of
     *                                       that name already exists <i>(optional specific exception)</i>
     * @throws IOException                   if an I/O error occurs or the parent directory does not exist
     * @throws SecurityException             In the case of the default provider, and a security manager is
     *                                       installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *                                       method is invoked to check write access to the new directory.
     */
    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {

    }

    /**
     * Deletes a file. This method works in exactly the  manner specified by the
     * {@link Files#delete} method.
     *
     * @param path the path to the file to delete
     * @throws NoSuchFileException        if the file does not exist <i>(optional specific exception)</i>
     * @throws DirectoryNotEmptyException if the file is a directory and could not otherwise be deleted
     *                                    because the directory is not empty <i>(optional specific
     *                                    exception)</i>
     * @throws IOException                if an I/O error occurs
     * @throws SecurityException          In the case of the default provider, and a security manager is
     *                                    installed, the {@link SecurityManager#checkDelete(String)} method
     *                                    is invoked to check delete access to the file
     */
    @Override
    public void delete(Path path) throws IOException {

    }

    /**
     * Copy a file to a target file. This method works in exactly the manner
     * specified by the {@link Files#copy(Path, Path, CopyOption[])} method
     * except that both the source and target paths must be associated with
     * this provider.
     *
     * @param source  the path to the file to copy
     * @param target  the path to the target file
     * @param options options specifying how the copy should be done
     * @throws UnsupportedOperationException if the array contains a copy option that is not supported
     * @throws FileAlreadyExistsException    if the target file exists but cannot be replaced because the
     *                                       {@code REPLACE_EXISTING} option is not specified <i>(optional
     *                                       specific exception)</i>
     * @throws DirectoryNotEmptyException    the {@code REPLACE_EXISTING} option is specified but the file
     *                                       cannot be replaced because it is a non-empty directory
     *                                       <i>(optional specific exception)</i>
     * @throws IOException                   if an I/O error occurs
     * @throws SecurityException             In the case of the default provider, and a security manager is
     *                                       installed, the {@link SecurityManager#checkRead(String) checkRead}
     *                                       method is invoked to check read access to the source file, the
     *                                       {@link SecurityManager#checkWrite(String) checkWrite} is invoked
     *                                       to check write access to the target file. If a symbolic link is
     *                                       copied the security manager is invoked to check {@link
     *                                       LinkPermission}{@code ("symbolic")}.
     */
    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {

    }

    /**
     * Move or rename a file to a target file. This method works in exactly the
     * manner specified by the {@link Files#move} method except that both the
     * source and target paths must be associated with this provider.
     *
     * @param source  the path to the file to move
     * @param target  the path to the target file
     * @param options options specifying how the move should be done
     * @throws UnsupportedOperationException   if the array contains a copy option that is not supported
     * @throws FileAlreadyExistsException      if the target file exists but cannot be replaced because the
     *                                         {@code REPLACE_EXISTING} option is not specified <i>(optional
     *                                         specific exception)</i>
     * @throws DirectoryNotEmptyException      the {@code REPLACE_EXISTING} option is specified but the file
     *                                         cannot be replaced because it is a non-empty directory
     *                                         <i>(optional specific exception)</i>
     * @throws AtomicMoveNotSupportedException if the options array contains the {@code ATOMIC_MOVE} option but
     *                                         the file cannot be moved as an atomic file system operation.
     * @throws IOException                     if an I/O error occurs
     * @throws SecurityException               In the case of the default provider, and a security manager is
     *                                         installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *                                         method is invoked to check write access to both the source and
     *                                         target file.
     */
    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {

    }

    /**
     * Tests if two paths locate the same file. This method works in exactly the
     * manner specified by the {@link Files#isSameFile} method.
     *
     * @param path  one path to the file
     * @param path2 the other path
     * @return {@code true} if, and only if, the two paths locate the same file
     * @throws IOException       if an I/O error occurs
     * @throws SecurityException In the case of the default provider, and a security manager is
     *                           installed, the {@link SecurityManager#checkRead(String) checkRead}
     *                           method is invoked to check read access to both files.
     */
    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return false;
    }

    /**
     * Tells whether or not a file is considered <em>hidden</em>. This method
     * works in exactly the manner specified by the {@link Files#isHidden}
     * method.
     *
     * <p> This method is invoked by the {@link Files#isHidden isHidden} method.
     *
     * @param path the path to the file to test
     * @return {@code true} if the file is considered hidden
     * @throws IOException       if an I/O error occurs
     * @throws SecurityException In the case of the default provider, and a security manager is
     *                           installed, the {@link SecurityManager#checkRead(String) checkRead}
     *                           method is invoked to check read access to the file.
     */
    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    /**
     * Returns the {@link FileStore} representing the file store where a file
     * is located. This method works in exactly the manner specified by the
     * {@link Files#getFileStore} method.
     *
     * @param path the path to the file
     * @return the file store where the file is stored
     * @throws IOException       if an I/O error occurs
     * @throws SecurityException In the case of the default provider, and a security manager is
     *                           installed, the {@link SecurityManager#checkRead(String) checkRead}
     *                           method is invoked to check read access to the file, and in
     *                           addition it checks
     *                           {@link RuntimePermission}{@code ("getFileStoreAttributes")}
     */
    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return null;
    }

    /**
     * Checks the existence, and optionally the accessibility, of a file.
     *
     * <p> This method may be used by the {@link Files#isReadable isReadable},
     * {@link Files#isWritable isWritable} and {@link Files#isExecutable
     * isExecutable} methods to check the accessibility of a file.
     *
     * <p> This method checks the existence of a file and that this Java virtual
     * machine has appropriate privileges that would allow it access the file
     * according to all of access modes specified in the {@code modes} parameter
     * as follows:
     *
     * <table class="striped">
     * <caption style="display:none">Access Modes</caption>
     * <thead>
     * <tr> <th scope="col">Value</th> <th scope="col">Description</th> </tr>
     * </thead>
     * <tbody>
     * <tr>
     *   <th scope="row"> {@link AccessMode#READ READ} </th>
     *   <td> Checks that the file exists and that the Java virtual machine has
     *     permission to read the file. </td>
     * </tr>
     * <tr>
     *   <th scope="row"> {@link AccessMode#WRITE WRITE} </th>
     *   <td> Checks that the file exists and that the Java virtual machine has
     *     permission to write to the file, </td>
     * </tr>
     * <tr>
     *   <th scope="row"> {@link AccessMode#EXECUTE EXECUTE} </th>
     *   <td> Checks that the file exists and that the Java virtual machine has
     *     permission to {@link Runtime#exec execute} the file. The semantics
     *     may differ when checking access to a directory. For example, on UNIX
     *     systems, checking for {@code EXECUTE} access checks that the Java
     *     virtual machine has permission to search the directory in order to
     *     access file or subdirectories. </td>
     * </tr>
     * </tbody>
     * </table>
     *
     * <p> If the {@code modes} parameter is of length zero, then the existence
     * of the file is checked.
     *
     * <p> This method follows symbolic links if the file referenced by this
     * object is a symbolic link. Depending on the implementation, this method
     * may require to read file permissions, access control lists, or other
     * file attributes in order to check the effective access to the file. To
     * determine the effective access to a file may require access to several
     * attributes and so in some implementations this method may not be atomic
     * with respect to other file system operations.
     *
     * @param path  the path to the file to check
     * @param modes The access modes to check; may have zero elements
     * @throws UnsupportedOperationException an implementation is required to support checking for
     *                                       {@code READ}, {@code WRITE}, and {@code EXECUTE} access. This
     *                                       exception is specified to allow for the {@code Access} enum to
     *                                       be extended in future releases.
     * @throws NoSuchFileException           if a file does not exist <i>(optional specific exception)</i>
     * @throws AccessDeniedException         the requested access would be denied or the access cannot be
     *                                       determined because the Java virtual machine has insufficient
     *                                       privileges or other reasons. <i>(optional specific exception)</i>
     * @throws IOException                   if an I/O error occurs
     * @throws SecurityException             In the case of the default provider, and a security manager is
     *                                       installed, the {@link SecurityManager#checkRead(String) checkRead}
     *                                       is invoked when checking read access to the file or only the
     *                                       existence of the file, the {@link SecurityManager#checkWrite(String)
     *                                       checkWrite} is invoked when checking write access to the file,
     *                                       and {@link SecurityManager#checkExec(String) checkExec} is invoked
     *                                       when checking execute access.
     */
    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        S3FileSystem s3FileSystem = (S3FileSystem) path.getFileSystem();
        S3Path s3Path = (S3Path) path;

        if (s3Client.listObjectsV2(s3FileSystem.getBucket(), s3Path.getS3Key()).getKeyCount() == 0) {
            throw new NoSuchFileException(path.toString());
        }
    }

    /**
     * Returns a file attribute view of a given type. This method works in
     * exactly the manner specified by the {@link Files#getFileAttributeView}
     * method.
     *
     * @param path    the path to the file
     * @param type    the {@code Class} object corresponding to the file attribute view
     * @param options options indicating how symbolic links are handled
     * @return a file attribute view of the specified type, or {@code null} if
     * the attribute view type is not available
     */
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return (V) new S3FileAttributeView();
    }

    /**
     * Reads a file's attributes as a bulk operation. This method works in
     * exactly the manner specified by the {@link
     * Files#readAttributes(Path, Class, LinkOption[])} method.
     *
     * @param path    the path to the file
     * @param type    the {@code Class} of the file attributes required
     *                to read
     * @param options options indicating how symbolic links are handled
     * @return the file attributes
     * @throws UnsupportedOperationException if an attributes of the given type are not supported
     * @throws IOException                   if an I/O error occurs
     * @throws SecurityException             In the case of the default provider, a security manager is
     *                                       installed, its {@link SecurityManager#checkRead(String) checkRead}
     *                                       method is invoked to check read access to the file
     */
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        S3FileSystem s3FileSystem = (S3FileSystem) path.getFileSystem();
        S3Path s3Path = (S3Path) path;

        try {
            ObjectMetadata objectMetadata = s3Client.getObjectMetadata(s3FileSystem.getBucket(), s3Path.getS3Key());
            return (A) new S3FileAttributes(objectMetadata, path.toString());
        } catch (AmazonS3Exception exception) {
            // Expected exception if the path represents a directory
        }

        if (s3Client.listObjectsV2(s3FileSystem.getBucket(), s3Path.getS3Key()).getKeyCount() > 0) {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            return (A) new S3FileAttributes(objectMetadata, path.toString());
        }

        throw new NoSuchFileException(path.toString());
    }

    /**
     * Reads a set of file attributes as a bulk operation. This method works in
     * exactly the manner specified by the {@link
     * Files#readAttributes(Path, String, LinkOption[])} method.
     *
     * @param path       the path to the file
     * @param attributes the attributes to read
     * @param options    options indicating how symbolic links are handled
     * @return a map of the attributes returned; may be empty. The map's keys
     * are the attribute names, its values are the attribute values
     * @throws UnsupportedOperationException if the attribute view is not available
     * @throws IllegalArgumentException      if no attributes are specified or an unrecognized attributes is
     *                                       specified
     * @throws IOException                   If an I/O error occurs
     * @throws SecurityException             In the case of the default provider, and a security manager is
     *                                       installed, its {@link SecurityManager#checkRead(String) checkRead}
     *                                       method denies read access to the file. If this method is invoked
     *                                       to read security sensitive attributes then the security manager
     *                                       may be invoke to check for additional permissions.
     */
    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return new HashMap<>();
    }

    /**
     * Sets the value of a file attribute. This method works in exactly the
     * manner specified by the {@link Files#setAttribute} method.
     *
     * @param path      the path to the file
     * @param attribute the attribute to set
     * @param value     the attribute value
     * @param options   options indicating how symbolic links are handled
     * @throws UnsupportedOperationException if the attribute view is not available
     * @throws IllegalArgumentException      if the attribute name is not specified, or is not recognized, or
     *                                       the attribute value is of the correct type but has an
     *                                       inappropriate value
     * @throws ClassCastException            If the attribute value is not of the expected type or is a
     *                                       collection containing elements that are not of the expected
     *                                       type
     * @throws IOException                   If an I/O error occurs
     * @throws SecurityException             In the case of the default provider, and a security manager is
     *                                       installed, its {@link SecurityManager#checkWrite(String) checkWrite}
     *                                       method denies write access to the file. If this method is invoked
     *                                       to set security sensitive attributes then the security manager
     *                                       may be invoked to check for additional permissions.
     */
    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {

    }
}
