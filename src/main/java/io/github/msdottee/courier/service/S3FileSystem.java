package io.github.msdottee.courier.service;

import com.amazonaws.services.s3.AmazonS3;

import java.io.File;
import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class S3FileSystem extends FileSystem {

    private final S3FileSystemProvider s3FileSystemProvider;

    private final AmazonS3 amazonS3;

    private final String bucket;

    public S3FileSystem(final S3FileSystemProvider s3FileSystemProvider, final AmazonS3 amazonS3, final String bucket) {
        this.s3FileSystemProvider = s3FileSystemProvider;
        this.amazonS3 = amazonS3;
        this.bucket = bucket;
    }

    /**
     * Returns the provider that created this file system.
     *
     * @return The provider that created this file system.
     */
    @Override
    public FileSystemProvider provider() {
        return s3FileSystemProvider;
    }

    /**
     * Closes this file system.
     *
     * <p> After a file system is closed then all subsequent access to the file
     * system, either by methods defined by this class or on objects associated
     * with this file system, throw {@link ClosedFileSystemException}. If the
     * file system is already closed then invoking this method has no effect.
     *
     * <p> Closing a file system will close all open {@link
     * Channel channels}, {@link DirectoryStream directory-streams},
     * {@link WatchService watch-service}, and other closeable objects associated
     * with this file system. The {@link FileSystems#getDefault default} file
     * system cannot be closed.
     *
     * @throws IOException                   If an I/O error occurs
     * @throws UnsupportedOperationException Thrown in the case of the default file system
     */
    @Override
    public void close() throws IOException {
        // S3 does not open any resources that need to be closed
    }

    /**
     * Tells whether or not this file system is open.
     *
     * <p> File systems created by the default provider are always open.
     *
     * @return {@code true} if, and only if, this file system is open
     */
    @Override
    public boolean isOpen() {
        // S3 is always open as long as the SDK is configured
        return true;
    }

    /**
     * Tells whether or not this file system allows only read-only access to
     * its file stores.
     *
     * @return {@code true} if, and only if, this file system provides
     * read-only access
     */
    @Override
    public boolean isReadOnly() {
        return false;
    }

    /**
     * Returns the name separator, represented as a string.
     *
     * <p> The name separator is used to separate names in a path string. An
     * implementation may support multiple name separators in which case this
     * method returns an implementation specific <em>default</em> name separator.
     * This separator is used when creating path strings by invoking the {@link
     * Path#toString() toString()} method.
     *
     * <p> In the case of the default provider, this method returns the same
     * separator as {@link File#separator}.
     *
     * @return The name separator
     */
    @Override
    public String getSeparator() {
        return "/";
    }

    /**
     * Returns an object to iterate over the paths of the root directories.
     *
     * <p> A file system provides access to a file store that may be composed
     * of a number of distinct file hierarchies, each with its own top-level
     * root directory. Unless denied by the security manager, each element in
     * the returned iterator corresponds to the root directory of a distinct
     * file hierarchy. The order of the elements is not defined. The file
     * hierarchies may change during the lifetime of the Java virtual machine.
     * For example, in some implementations, the insertion of removable media
     * may result in the creation of a new file hierarchy with its own
     * top-level directory.
     *
     * <p> When a security manager is installed, it is invoked to check access
     * to the each root directory. If denied, the root directory is not returned
     * by the iterator. In the case of the default provider, the {@link
     * SecurityManager#checkRead(String)} method is invoked to check read access
     * to each root directory. It is system dependent if the permission checks
     * are done when the iterator is obtained or during iteration.
     *
     * @return An object to iterate over the root directories
     */
    @Override
    public Iterable<Path> getRootDirectories() {
        return null;
    }

    /**
     * Returns an object to iterate over the underlying file stores.
     *
     * <p> The elements of the returned iterator are the {@link
     * FileStore FileStores} for this file system. The order of the elements is
     * not defined and the file stores may change during the lifetime of the
     * Java virtual machine. When an I/O error occurs, perhaps because a file
     * store is not accessible, then it is not returned by the iterator.
     *
     * <p> In the case of the default provider, and a security manager is
     * installed, the security manager is invoked to check {@link
     * RuntimePermission}{@code ("getFileStoreAttributes")}. If denied, then
     * no file stores are returned by the iterator. In addition, the security
     * manager's {@link SecurityManager#checkRead(String)} method is invoked to
     * check read access to the file store's <em>top-most</em> directory. If
     * denied, the file store is not returned by the iterator. It is system
     * dependent if the permission checks are done when the iterator is obtained
     * or during iteration.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to print the space usage for all file stores:
     * <pre>
     *     for (FileStore store: FileSystems.getDefault().getFileStores()) {
     *         long total = store.getTotalSpace() / 1024;
     *         long used = (store.getTotalSpace() - store.getUnallocatedSpace()) / 1024;
     *         long avail = store.getUsableSpace() / 1024;
     *         System.out.format("%-20s %12d %12d %12d%n", store, total, used, avail);
     *     }
     * </pre>
     *
     * @return An object to iterate over the backing file stores
     */
    @Override
    public Iterable<FileStore> getFileStores() {
        return null;
    }

    /**
     * Returns the set of the {@link FileAttributeView#name names} of the file
     * attribute views supported by this {@code FileSystem}.
     *
     * <p> The {@link BasicFileAttributeView} is required to be supported and
     * therefore the set contains at least one element, "basic".
     *
     * <p> The {@link FileStore#supportsFileAttributeView(String)
     * supportsFileAttributeView(String)} method may be used to test if an
     * underlying {@link FileStore} supports the file attributes identified by a
     * file attribute view.
     *
     * @return An unmodifiable set of the names of the supported file attribute
     * views
     */
    @Override
    public Set<String> supportedFileAttributeViews() {
        return new HashSet<>() {{
            add("basic");
            add("posix");
        }};
    }

    /**
     * Converts a path string, or a sequence of strings that when joined form
     * a path string, to a {@code Path}. If {@code more} does not specify any
     * elements then the value of the {@code first} parameter is the path string
     * to convert. If {@code more} specifies one or more elements then each
     * non-empty string, including {@code first}, is considered to be a sequence
     * of name elements (see {@link Path}) and is joined to form a path string.
     * The details as to how the Strings are joined is provider specific but
     * typically they will be joined using the {@link #getSeparator
     * name-separator} as the separator. For example, if the name separator is
     * "{@code /}" and {@code getPath("/foo","bar","gus")} is invoked, then the
     * path string {@code "/foo/bar/gus"} is converted to a {@code Path}.
     * A {@code Path} representing an empty path is returned if {@code first}
     * is the empty string and {@code more} does not contain any non-empty
     * strings.
     *
     * <p> The parsing and conversion to a path object is inherently
     * implementation dependent. In the simplest case, the path string is rejected,
     * and {@link InvalidPathException} thrown, if the path string contains
     * characters that cannot be converted to characters that are <em>legal</em>
     * to the file store. For example, on UNIX systems, the NUL (&#92;u0000)
     * character is not allowed to be present in a path. An implementation may
     * choose to reject path strings that contain names that are longer than those
     * allowed by any file store, and where an implementation supports a complex
     * path syntax, it may choose to reject path strings that are <em>badly
     * formed</em>.
     *
     * <p> In the case of the default provider, path strings are parsed based
     * on the definition of paths at the platform or virtual file system level.
     * For example, an operating system may not allow specific characters to be
     * present in a file name, but a specific underlying file store may impose
     * different or additional restrictions on the set of legal
     * characters.
     *
     * <p> This method throws {@link InvalidPathException} when the path string
     * cannot be converted to a path. Where possible, and where applicable,
     * the exception is created with an {@link InvalidPathException#getIndex
     * index} value indicating the first position in the {@code path} parameter
     * that caused the path string to be rejected.
     *
     * @param first the path string or initial part of the path string
     * @param more  additional strings to be joined to form the path string
     * @return the resulting {@code Path}
     * @throws InvalidPathException If the path string cannot be converted
     */
    @Override
    public Path getPath(String first, String... more) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(first);

        for (String additional : more ) {
            pathBuilder.append(getSeparator());
            pathBuilder.append(additional);
        }

        return new S3Path(this, pathBuilder.toString());
    }

    /**
     * Returns a {@code PathMatcher} that performs match operations on the
     * {@code String} representation of {@link Path} objects by interpreting a
     * given pattern.
     * <p>
     * The {@code syntaxAndPattern} parameter identifies the syntax and the
     * pattern and takes the form:
     * <blockquote><pre>
     * <i>syntax</i><b>:</b><i>pattern</i>
     * </pre></blockquote>
     * where {@code ':'} stands for itself.
     *
     * <p> A {@code FileSystem} implementation supports the "{@code glob}" and
     * "{@code regex}" syntaxes, and may support others. The value of the syntax
     * component is compared without regard to case.
     *
     * <p> When the syntax is "{@code glob}" then the {@code String}
     * representation of the path is matched using a limited pattern language
     * that resembles regular expressions but with a simpler syntax. For example:
     *
     * <table class="striped" style="text-align:left; margin-left:2em">
     * <caption style="display:none">Pattern Language</caption>
     * <thead>
     * <tr>
     *   <th scope="col">Example
     *   <th scope="col">Description
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     *   <th scope="row">{@code *.java}</th>
     *   <td>Matches a path that represents a file name ending in {@code .java}</td>
     * </tr>
     * <tr>
     *   <th scope="row">{@code *.*}</th>
     *   <td>Matches file names containing a dot</td>
     * </tr>
     * <tr>
     *   <th scope="row">{@code *.{java,class}}</th>
     *   <td>Matches file names ending with {@code .java} or {@code .class}</td>
     * </tr>
     * <tr>
     *   <th scope="row">{@code foo.?}</th>
     *   <td>Matches file names starting with {@code foo.} and a single
     *   character extension</td>
     * </tr>
     * <tr>
     *   <th scope="row"><code>&#47;home&#47;*&#47;*</code>
     *   <td>Matches <code>&#47;home&#47;gus&#47;data</code> on UNIX platforms</td>
     * </tr>
     * <tr>
     *   <th scope="row"><code>&#47;home&#47;**</code>
     *   <td>Matches <code>&#47;home&#47;gus</code> and
     *   <code>&#47;home&#47;gus&#47;data</code> on UNIX platforms</td>
     * </tr>
     * <tr>
     *   <th scope="row"><code>C:&#92;&#92;*</code>
     *   <td>Matches <code>C:&#92;foo</code> and <code>C:&#92;bar</code> on the Windows
     *   platform (note that the backslash is escaped; as a string literal in the
     *   Java Language the pattern would be <code>"C:&#92;&#92;&#92;&#92;*"</code>) </td>
     * </tr>
     * </tbody>
     * </table>
     *
     * <p> The following rules are used to interpret glob patterns:
     *
     * <ul>
     *   <li><p> The {@code *} character matches zero or more {@link Character
     *   characters} of a {@link Path#getName(int) name} component without
     *   crossing directory boundaries. </p></li>
     *
     *   <li><p> The {@code **} characters matches zero or more {@link Character
     *   characters} crossing directory boundaries. </p></li>
     *
     *   <li><p> The {@code ?} character matches exactly one character of a
     *   name component.</p></li>
     *
     *   <li><p> The backslash character ({@code \}) is used to escape characters
     *   that would otherwise be interpreted as special characters. The expression
     *   {@code \\} matches a single backslash and "\{" matches a left brace
     *   for example.  </p></li>
     *
     *   <li><p> The {@code [ ]} characters are a <i>bracket expression</i> that
     *   match a single character of a name component out of a set of characters.
     *   For example, {@code [abc]} matches {@code "a"}, {@code "b"}, or {@code "c"}.
     *   The hyphen ({@code -}) may be used to specify a range so {@code [a-z]}
     *   specifies a range that matches from {@code "a"} to {@code "z"} (inclusive).
     *   These forms can be mixed so [abce-g] matches {@code "a"}, {@code "b"},
     *   {@code "c"}, {@code "e"}, {@code "f"} or {@code "g"}. If the character
     *   after the {@code [} is a {@code !} then it is used for negation so {@code
     *   [!a-c]} matches any character except {@code "a"}, {@code "b"}, or {@code
     *   "c"}.
     *   <p> Within a bracket expression the {@code *}, {@code ?} and {@code \}
     *   characters match themselves. The ({@code -}) character matches itself if
     *   it is the first character within the brackets, or the first character
     *   after the {@code !} if negating.</p></li>
     *
     *   <li><p> The {@code { }} characters are a group of subpatterns, where
     *   the group matches if any subpattern in the group matches. The {@code ","}
     *   character is used to separate the subpatterns. Groups cannot be nested.
     *   </p></li>
     *
     *   <li><p> Leading period<code>&#47;</code>dot characters in file name are
     *   treated as regular characters in match operations. For example,
     *   the {@code "*"} glob pattern matches file name {@code ".login"}.
     *   The {@link Files#isHidden} method may be used to test whether a file
     *   is considered hidden.
     *   </p></li>
     *
     *   <li><p> All other characters match themselves in an implementation
     *   dependent manner. This includes characters representing any {@link
     *   FileSystem#getSeparator name-separators}. </p></li>
     *
     *   <li><p> The matching of {@link Path#getRoot root} components is highly
     *   implementation-dependent and is not specified. </p></li>
     *
     * </ul>
     *
     * <p> When the syntax is "{@code regex}" then the pattern component is a
     * regular expression as defined by the {@link Pattern}
     * class.
     *
     * <p>  For both the glob and regex syntaxes, the matching details, such as
     * whether the matching is case sensitive, are implementation-dependent
     * and therefore not specified.
     *
     * @param syntaxAndPattern The syntax and pattern
     * @return A path matcher that may be used to match paths against the pattern
     * @throws IllegalArgumentException      If the parameter does not take the form: {@code syntax:pattern}
     * @throws PatternSyntaxException        If the pattern is invalid
     * @throws UnsupportedOperationException If the pattern syntax is not known to the implementation
     * @see Files#newDirectoryStream(Path, String)
     */
    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return null;
    }

    /**
     * Returns the {@code UserPrincipalLookupService} for this file system
     * <i>(optional operation)</i>. The resulting lookup service may be used to
     * lookup user or group names.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to make "joe" the owner of a file:
     * <pre>
     *     UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
     *     Files.setOwner(path, lookupService.lookupPrincipalByName("joe"));
     * </pre>
     *
     * @return The {@code UserPrincipalLookupService} for this file system
     * @throws UnsupportedOperationException If this {@code FileSystem} does not does have a lookup service
     */
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return null;
    }

    /**
     * Constructs a new {@link WatchService} <i>(optional operation)</i>.
     *
     * <p> This method constructs a new watch service that may be used to watch
     * registered objects for changes and events.
     *
     * @return a new watch service
     * @throws UnsupportedOperationException If this {@code FileSystem} does not support watching file system
     *                                       objects for changes and events. This exception is not thrown
     *                                       by {@code FileSystems} created by the default provider.
     * @throws IOException                   If an I/O error occurs
     */
    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException("S3 does not support file watching.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        S3FileSystem that = (S3FileSystem) o;
        return Objects.equals(s3FileSystemProvider, that.s3FileSystemProvider) &&
                Objects.equals(amazonS3, that.amazonS3) &&
                Objects.equals(bucket, that.bucket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(s3FileSystemProvider, amazonS3, bucket);
    }
}
