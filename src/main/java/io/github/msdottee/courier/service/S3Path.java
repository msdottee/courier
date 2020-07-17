package io.github.msdottee.courier.service;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Objects;

public class S3Path implements Path {

    private static final String ROOT = "/";

    private static final char SEPARATOR = '/';

    private final FileSystem fileSystem;

    private final String path;

    public S3Path(FileSystem fileSystem, String path) {
        this.fileSystem = fileSystem;
        this.path = path;
    }

    /**
     * Returns the file system that created this object.
     *
     * @return the file system that created this object
     */
    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * Tells whether or not this path is absolute.
     *
     * <p> An absolute path is complete in that it doesn't need to be combined
     * with other path information in order to locate a file.
     *
     * @return {@code true} if, and only if, this path is absolute
     */
    @Override
    public boolean isAbsolute() {
        return path.startsWith(ROOT);
    }

    /**
     * Returns the root component of this path as a {@code Path} object,
     * or {@code null} if this path does not have a root component.
     *
     * @return a path representing the root component of this path,
     * or {@code null}
     */
    @Override
    public Path getRoot() {
        return path.startsWith(ROOT) ? new S3Path(getFileSystem(), ROOT) : null;
    }

    /**
     * Returns the name of the file or directory denoted by this path as a
     * {@code Path} object. The file name is the <em>farthest</em> element from
     * the root in the directory hierarchy.
     *
     * @return a path representing the name of the file or directory, or
     * {@code null} if this path has zero elements
     */
    @Override
    public Path getFileName() {
        if (path.equals(ROOT)) {
            return null;
        }

        int offset = path.lastIndexOf(SEPARATOR);

        return new S3Path(fileSystem, path.substring(offset + 1));
    }

    /**
     * Returns the <em>parent path</em>, or {@code null} if this path does not
     * have a parent.
     *
     * <p> The parent of this path object consists of this path's root
     * component, if any, and each element in the path except for the
     * <em>farthest</em> from the root in the directory hierarchy. This method
     * does not access the file system; the path or its parent may not exist.
     * Furthermore, this method does not eliminate special names such as "."
     * and ".." that may be used in some implementations. On UNIX for example,
     * the parent of "{@code /a/b/c}" is "{@code /a/b}", and the parent of
     * {@code "x/y/.}" is "{@code x/y}". This method may be used with the {@link
     * #normalize normalize} method, to eliminate redundant names, for cases where
     * <em>shell-like</em> navigation is required.
     *
     * <p> If this path has more than one element, and no root component, then
     * this method is equivalent to evaluating the expression:
     * <blockquote><pre>
     * subpath(0,&nbsp;getNameCount()-1);
     * </pre></blockquote>
     *
     * @return a path representing the path's parent
     */
    @Override
    public Path getParent() {
        if (path.equals(ROOT)) {
            return null;
        }

        int offset = path.lastIndexOf(SEPARATOR);

        if (offset == -1) {
            return null;
        }

        if (offset == 0) {
            return new S3Path(fileSystem, ROOT);
        }

        return new S3Path(fileSystem, path.substring(0, offset));
    }

    /**
     * Returns the number of name elements in the path.
     *
     * @return the number of elements in the path, or {@code 0} if this path
     * only represents a root component
     */
    @Override
    public int getNameCount() {
        return 0;
    }

    /**
     * Returns a name element of this path as a {@code Path} object.
     *
     * <p> The {@code index} parameter is the index of the name element to return.
     * The element that is <em>closest</em> to the root in the directory hierarchy
     * has index {@code 0}. The element that is <em>farthest</em> from the root
     * has index {@link #getNameCount count}{@code -1}.
     *
     * @param index the index of the element
     * @return the name element
     * @throws IllegalArgumentException if {@code index} is negative, {@code index} is greater than or
     *                                  equal to the number of elements, or this path has zero name
     *                                  elements
     */
    @Override
    public Path getName(int index) {
        String[] pathPieces = path.split(getFileSystem().getSeparator());

        if (index < 0) {
            throw new IllegalArgumentException("Index is negative.");
        }

        if (index > pathPieces.length - 1) {
            throw new IllegalArgumentException("Index is greater than the number of path elements.");
        }

        if (path.equals(getFileSystem().getSeparator())) {
            throw new IllegalArgumentException("Path has zero elements.");
        }

        return new S3Path(getFileSystem(), pathPieces[index]);
    }

    /**
     * Returns a relative {@code Path} that is a subsequence of the name
     * elements of this path.
     *
     * <p> The {@code beginIndex} and {@code endIndex} parameters specify the
     * subsequence of name elements. The name that is <em>closest</em> to the root
     * in the directory hierarchy has index {@code 0}. The name that is
     * <em>farthest</em> from the root has index {@link #getNameCount
     * count}{@code -1}. The returned {@code Path} object has the name elements
     * that begin at {@code beginIndex} and extend to the element at index {@code
     * endIndex-1}.
     *
     * @param beginIndex the index of the first element, inclusive
     * @param endIndex   the index of the last element, exclusive
     * @return a new {@code Path} object that is a subsequence of the name
     * elements in this {@code Path}
     * @throws IllegalArgumentException if {@code beginIndex} is negative, or greater than or equal to
     *                                  the number of elements. If {@code endIndex} is less than or
     *                                  equal to {@code beginIndex}, or larger than the number of elements.
     */
    @Override
    public Path subpath(int beginIndex, int endIndex) {
        String[] pathPieces = path.split(getFileSystem().getSeparator());

        if (beginIndex < 0) {
            throw new IllegalArgumentException("Begin index is negative.");
        }

        if (endIndex < 0) {
            throw new IllegalArgumentException("End index is negative");
        }

        if (beginIndex > pathPieces.length - 1) {
            throw new IllegalArgumentException("Begin index is greater than the number of path elements.");
        }

        if (endIndex > pathPieces.length - 1) {
            throw new IllegalArgumentException("End index is greater than the number of path elements.");
        }

        if (beginIndex > endIndex) {
            throw new IllegalArgumentException("Begin index is greater than the end index.");
        }

        if (path.equals(getFileSystem().getSeparator())) {
            throw new IllegalArgumentException("Path has zero elements.");
        }

        StringBuilder subpath = new StringBuilder();

        for (int i = beginIndex; i < endIndex; i++) {
            subpath.append(pathPieces[i]);

            if (i < endIndex - 1) {
                subpath.append(getFileSystem().getSeparator());
            }
        }

        return new S3Path(getFileSystem(), subpath.toString());
    }

    /**
     * Tests if this path starts with the given path.
     *
     * <p> This path <em>starts</em> with the given path if this path's root
     * component <em>starts</em> with the root component of the given path,
     * and this path starts with the same name elements as the given path.
     * If the given path has more name elements than this path then {@code false}
     * is returned.
     *
     * <p> Whether or not the root component of this path starts with the root
     * component of the given path is file system specific. If this path does
     * not have a root component and the given path has a root component then
     * this path does not start with the given path.
     *
     * <p> If the given path is associated with a different {@code FileSystem}
     * to this path then {@code false} is returned.
     *
     * @param other the given path
     * @return {@code true} if this path starts with the given path; otherwise
     * {@code false}
     */
    @Override
    public boolean startsWith(Path other) {
        return path.startsWith(other.toString());
    }

    /**
     * Tests if this path ends with the given path.
     *
     * <p> If the given path has <em>N</em> elements, and no root component,
     * and this path has <em>N</em> or more elements, then this path ends with
     * the given path if the last <em>N</em> elements of each path, starting at
     * the element farthest from the root, are equal.
     *
     * <p> If the given path has a root component then this path ends with the
     * given path if the root component of this path <em>ends with</em> the root
     * component of the given path, and the corresponding elements of both paths
     * are equal. Whether or not the root component of this path ends with the
     * root component of the given path is file system specific. If this path
     * does not have a root component and the given path has a root component
     * then this path does not end with the given path.
     *
     * <p> If the given path is associated with a different {@code FileSystem}
     * to this path then {@code false} is returned.
     *
     * @param other the given path
     * @return {@code true} if this path ends with the given path; otherwise
     * {@code false}
     */
    @Override
    public boolean endsWith(Path other) {
        return path.endsWith(other.toString());
    }

    /**
     * Returns a path that is this path with redundant name elements eliminated.
     *
     * <p> The precise definition of this method is implementation dependent but
     * in general it derives from this path, a path that does not contain
     * <em>redundant</em> name elements. In many file systems, the "{@code .}"
     * and "{@code ..}" are special names used to indicate the current directory
     * and parent directory. In such file systems all occurrences of "{@code .}"
     * are considered redundant. If a "{@code ..}" is preceded by a
     * non-"{@code ..}" name then both names are considered redundant (the
     * process to identify such names is repeated until it is no longer
     * applicable).
     *
     * <p> This method does not access the file system; the path may not locate
     * a file that exists. Eliminating "{@code ..}" and a preceding name from a
     * path may result in the path that locates a different file than the original
     * path. This can arise when the preceding name is a symbolic link.
     *
     * @return the resulting path or this path if it does not contain
     * redundant name elements; an empty path is returned if this path
     * does not have a root component and all name elements are redundant
     * @see #getParent
     * @see #toRealPath
     */
    @Override
    public Path normalize() {
        return this;
    }

    /**
     * Resolve the given path against this path.
     *
     * <p> If the {@code other} parameter is an {@link #isAbsolute() absolute}
     * path then this method trivially returns {@code other}. If {@code other}
     * is an <i>empty path</i> then this method trivially returns this path.
     * Otherwise this method considers this path to be a directory and resolves
     * the given path against this path. In the simplest case, the given path
     * does not have a {@link #getRoot root} component, in which case this method
     * <em>joins</em> the given path to this path and returns a resulting path
     * that {@link #endsWith ends} with the given path. Where the given path has
     * a root component then resolution is highly implementation dependent and
     * therefore unspecified.
     *
     * @param other the path to resolve against this path
     * @return the resulting path
     * @see #relativize
     */
    @Override
    public Path resolve(Path other) {
        return this;
    }

    /**
     * Constructs a relative path between this path and a given path.
     *
     * <p> Relativization is the inverse of {@link #resolve(Path) resolution}.
     * This method attempts to construct a {@link #isAbsolute relative} path
     * that when {@link #resolve(Path) resolved} against this path, yields a
     * path that locates the same file as the given path. For example, on UNIX,
     * if this path is {@code "/a/b"} and the given path is {@code "/a/b/c/d"}
     * then the resulting relative path would be {@code "c/d"}. Where this
     * path and the given path do not have a {@link #getRoot root} component,
     * then a relative path can be constructed. A relative path cannot be
     * constructed if only one of the paths have a root component. Where both
     * paths have a root component then it is implementation dependent if a
     * relative path can be constructed. If this path and the given path are
     * {@link #equals equal} then an <i>empty path</i> is returned.
     *
     * <p> For any two {@link #normalize normalized} paths <i>p</i> and
     * <i>q</i>, where <i>q</i> does not have a root component,
     * <blockquote>
     * <i>p</i>{@code .relativize(}<i>p</i>
     * {@code .resolve(}<i>q</i>{@code )).equals(}<i>q</i>{@code )}
     * </blockquote>
     *
     * <p> When symbolic links are supported, then whether the resulting path,
     * when resolved against this path, yields a path that can be used to locate
     * the {@link Files#isSameFile same} file as {@code other} is implementation
     * dependent. For example, if this path is  {@code "/a/b"} and the given
     * path is {@code "/a/x"} then the resulting relative path may be {@code
     * "../x"}. If {@code "b"} is a symbolic link then is implementation
     * dependent if {@code "a/b/../x"} would locate the same file as {@code "/a/x"}.
     *
     * @param other the path to relativize against this path
     * @return the resulting relative path, or an empty path if both paths are
     * equal
     * @throws IllegalArgumentException if {@code other} is not a {@code Path} that can be relativized
     *                                  against this path
     */
    @Override
    public Path relativize(Path other) {
        return null;
    }

    /**
     * Returns a URI to represent this path.
     *
     * <p> This method constructs an absolute {@link URI} with a {@link
     * URI#getScheme() scheme} equal to the URI scheme that identifies the
     * provider. The exact form of the scheme specific part is highly provider
     * dependent.
     *
     * <p> In the case of the default provider, the URI is hierarchical with
     * a {@link URI#getPath() path} component that is absolute. The query and
     * fragment components are undefined. Whether the authority component is
     * defined or not is implementation dependent. There is no guarantee that
     * the {@code URI} may be used to construct a {@link File java.io.File}.
     * In particular, if this path represents a Universal Naming Convention (UNC)
     * path, then the UNC server name may be encoded in the authority component
     * of the resulting URI. In the case of the default provider, and the file
     * exists, and it can be determined that the file is a directory, then the
     * resulting {@code URI} will end with a slash.
     *
     * <p> The default provider provides a similar <em>round-trip</em> guarantee
     * to the {@link File} class. For a given {@code Path} <i>p</i> it
     * is guaranteed that
     * <blockquote>
     * {@link Path#of(URI) Path.of}{@code (}<i>p</i>{@code .toUri()).equals(}<i>p</i>
     * {@code .}{@link #toAbsolutePath() toAbsolutePath}{@code ())}
     * </blockquote>
     * so long as the original {@code Path}, the {@code URI}, and the new {@code
     * Path} are all created in (possibly different invocations of) the same
     * Java virtual machine. Whether other providers make any guarantees is
     * provider specific and therefore unspecified.
     *
     * <p> When a file system is constructed to access the contents of a file
     * as a file system then it is highly implementation specific if the returned
     * URI represents the given path in the file system or it represents a
     * <em>compound</em> URI that encodes the URI of the enclosing file system.
     * A format for compound URIs is not defined in this release; such a scheme
     * may be added in a future release.
     *
     * @return the URI representing this path
     * @throws IOError           if an I/O error occurs obtaining the absolute path, or where a
     *                           file system is constructed to access the contents of a file as
     *                           a file system, and the URI of the enclosing file system cannot be
     *                           obtained
     * @throws SecurityException In the case of the default provider, and a security manager
     *                           is installed, the {@link #toAbsolutePath toAbsolutePath} method
     *                           throws a security exception.
     */
    @Override
    public URI toUri() {
        return null;
    }

    /**
     * Returns a {@code Path} object representing the absolute path of this
     * path.
     *
     * <p> If this path is already {@link Path#isAbsolute absolute} then this
     * method simply returns this path. Otherwise, this method resolves the path
     * in an implementation dependent manner, typically by resolving the path
     * against a file system default directory. Depending on the implementation,
     * this method may throw an I/O error if the file system is not accessible.
     *
     * @return a {@code Path} object representing the absolute path
     * @throws IOError           if an I/O error occurs
     * @throws SecurityException In the case of the default provider, a security manager
     *                           is installed, and this path is not absolute, then the security
     *                           manager's {@link SecurityManager#checkPropertyAccess(String)
     *                           checkPropertyAccess} method is invoked to check access to the
     *                           system property {@code user.dir}
     */
    @Override
    public Path toAbsolutePath() {
        return this;
    }

    /**
     * Returns the <em>real</em> path of an existing file.
     *
     * <p> The precise definition of this method is implementation dependent but
     * in general it derives from this path, an {@link #isAbsolute absolute}
     * path that locates the {@link Files#isSameFile same} file as this path, but
     * with name elements that represent the actual name of the directories
     * and the file. For example, where filename comparisons on a file system
     * are case insensitive then the name elements represent the names in their
     * actual case. Additionally, the resulting path has redundant name
     * elements removed.
     *
     * <p> If this path is relative then its absolute path is first obtained,
     * as if by invoking the {@link #toAbsolutePath toAbsolutePath} method.
     *
     * <p> The {@code options} array may be used to indicate how symbolic links
     * are handled. By default, symbolic links are resolved to their final
     * target. If the option {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} is
     * present then this method does not resolve symbolic links.
     * <p>
     * Some implementations allow special names such as "{@code ..}" to refer to
     * the parent directory. When deriving the <em>real path</em>, and a
     * "{@code ..}" (or equivalent) is preceded by a non-"{@code ..}" name then
     * an implementation will typically cause both names to be removed. When
     * not resolving symbolic links and the preceding name is a symbolic link
     * then the names are only removed if it guaranteed that the resulting path
     * will locate the same file as this path.
     *
     * @param options options indicating how symbolic links are handled
     * @return an absolute path represent the <em>real</em> path of the file
     * located by this object
     * @throws IOException       if the file does not exist or an I/O error occurs
     * @throws SecurityException In the case of the default provider, and a security manager
     *                           is installed, its {@link SecurityManager#checkRead(String) checkRead}
     *                           method is invoked to check read access to the file, and where
     *                           this path is not absolute, its {@link SecurityManager#checkPropertyAccess(String)
     *                           checkPropertyAccess} method is invoked to check access to the
     *                           system property {@code user.dir}
     */
    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return null;
    }

    /**
     * Registers the file located by this path with a watch service.
     *
     * <p> In this release, this path locates a directory that exists. The
     * directory is registered with the watch service so that entries in the
     * directory can be watched. The {@code events} parameter is the events to
     * register and may contain the following events:
     * <ul>
     *   <li>{@link StandardWatchEventKinds#ENTRY_CREATE ENTRY_CREATE} -
     *       entry created or moved into the directory</li>
     *   <li>{@link StandardWatchEventKinds#ENTRY_DELETE ENTRY_DELETE} -
     *        entry deleted or moved out of the directory</li>
     *   <li>{@link StandardWatchEventKinds#ENTRY_MODIFY ENTRY_MODIFY} -
     *        entry in directory was modified</li>
     * </ul>
     *
     * <p> The {@link WatchEvent#context context} for these events is the
     * relative path between the directory located by this path, and the path
     * that locates the directory entry that is created, deleted, or modified.
     *
     * <p> The set of events may include additional implementation specific
     * event that are not defined by the enum {@link StandardWatchEventKinds}
     *
     * <p> The {@code modifiers} parameter specifies <em>modifiers</em> that
     * qualify how the directory is registered. This release does not define any
     * <em>standard</em> modifiers. It may contain implementation specific
     * modifiers.
     *
     * <p> Where a file is registered with a watch service by means of a symbolic
     * link then it is implementation specific if the watch continues to depend
     * on the existence of the symbolic link after it is registered.
     *
     * @param watcher   the watch service to which this object is to be registered
     * @param events    the events for which this object should be registered
     * @param modifiers the modifiers, if any, that modify how the object is registered
     * @return a key representing the registration of this object with the
     * given watch service
     * @throws UnsupportedOperationException if unsupported events or modifiers are specified
     * @throws IllegalArgumentException      if an invalid combination of events or modifiers is specified
     * @throws ClosedWatchServiceException   if the watch service is closed
     * @throws NotDirectoryException         if the file is registered to watch the entries in a directory
     *                                       and the file is not a directory  <i>(optional specific exception)</i>
     * @throws IOException                   if an I/O error occurs
     * @throws SecurityException             In the case of the default provider, and a security manager is
     *                                       installed, the {@link SecurityManager#checkRead(String) checkRead}
     *                                       method is invoked to check read access to the file.
     */
    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("File watching is not supported by S3 paths.");
    }

    /**
     * Compares two abstract paths lexicographically. The ordering defined by
     * this method is provider specific, and in the case of the default
     * provider, platform specific. This method does not access the file system
     * and neither file is required to exist.
     *
     * <p> This method may not be used to compare paths that are associated
     * with different file system providers.
     *
     * @param other the path compared to this path.
     * @return zero if the argument is {@link #equals equal} to this path, a
     * value less than zero if this path is lexicographically less than
     * the argument, or a value greater than zero if this path is
     * lexicographically greater than the argument
     * @throws ClassCastException if the paths are associated with different providers
     */
    @Override
    public int compareTo(Path other) {
        S3Path otherS3 = (S3Path) other;
        return path.compareTo(otherS3.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        S3Path paths = (S3Path) o;
        return Objects.equals(fileSystem, paths.fileSystem) &&
                Objects.equals(path, paths.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileSystem, path);
    }

    @Override
    public String toString() {
        return "S3Path{" +
                "path='" + path + '\'' +
                '}' ;
    }
}
