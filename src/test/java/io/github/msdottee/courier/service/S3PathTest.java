package io.github.msdottee.courier.service;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class S3PathTest {

    private static final S3FileSystem S3_FILE_SYSTEM =
            new S3FileSystem(null, null, "bucket");

    @Test
    public void ensureGetFileSystemReturnsParentFileSystem() {
        Path path = S3_FILE_SYSTEM.getPath("test");

        assertThat(path.getFileSystem()).isSameAs(S3_FILE_SYSTEM);
    }

    @Test
    public void ensureIsAbsoluteIsTrueWhenPathStartsWithForwardSlash() {
        Path path = S3_FILE_SYSTEM.getPath("/absolute/path");

        assertThat(path.isAbsolute()).isTrue();
    }

    @Test
    public void ensureIsAbsoluteIsFalseWhenPathDoesNotStartWithForwardSlash() {
        Path path = S3_FILE_SYSTEM.getPath("relative/path");

        assertThat(path.isAbsolute()).isFalse();
    }

    @Test
    public void ensureGetRootReturnsRootPathForAbsolutePath() {
        Path path = S3_FILE_SYSTEM.getPath("/absolute/path");

        assertThat(path.getRoot()).isEqualTo(S3_FILE_SYSTEM.getPath("/"));
    }

    @Test
    public void ensureGetRootReturnsNullForRelativePath() {
        Path path = S3_FILE_SYSTEM.getPath("relative/path");

        assertThat(path.getRoot()).isNull();
    }

    @Test
    public void ensureGetFileNameReturnsFileNameForPath() {
        Path path = S3_FILE_SYSTEM.getPath("/a");

        assertThat(path.getFileName()).isEqualTo(S3_FILE_SYSTEM.getPath("a"));
    }

    @Test
    public void ensureGetFileNameReturnsFileNameForNestedPath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.getFileName()).isEqualTo(S3_FILE_SYSTEM.getPath("c"));
    }

    @Test
    public void ensureGetFileNameReturnsNullForRootPath() {
        Path path = S3_FILE_SYSTEM.getPath("/");

        assertThat(path.getFileName()).isNull();
    }

    @Test
    public void ensureGetFileNameReturnsNullForRelativePath() {
        Path path = S3_FILE_SYSTEM.getPath("a/b/c");

        assertThat(path.getFileName()).isEqualTo(S3_FILE_SYSTEM.getPath("c"));
    }

    @Test
    public void ensureGetParentReturnsParentForAbsolutePath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.getParent()).isEqualTo(S3_FILE_SYSTEM.getPath("/a/b"));
    }

    @Test
    public void ensureGetParentReturnsParentForRelativePath() {
        Path path = S3_FILE_SYSTEM.getPath("a/b/c");

        assertThat(path.getParent()).isEqualTo(S3_FILE_SYSTEM.getPath("a/b"));
    }

    @Test
    public void ensureGetParentTreatsSpecialNamesAsNormalNames() {
        Path path = S3_FILE_SYSTEM.getPath("/a/../c");

        assertThat(path.getParent()).isEqualTo(S3_FILE_SYSTEM.getPath("/a/.."));
    }

    @Test
    public void ensureGetParentReturnsRootForFileInRootDirectory() {
        Path path = S3_FILE_SYSTEM.getPath("/a");

        assertThat(path.getParent()).isEqualTo(S3_FILE_SYSTEM.getPath("/"));
    }

    @Test
    public void ensureGetParentReturnsNullForRootDirectory() {
        Path path = S3_FILE_SYSTEM.getPath("/");

        assertThat(path.getParent()).isNull();
    }

    @Test
    public void ensureGetParentReturnsNullForEmptyPath() {
        Path path = S3_FILE_SYSTEM.getPath("");

        assertThat(path.getParent()).isNull();
    }

    @Test
    public void ensureGetParentReturnsNullForRelativePathWithNoParent() {
        Path path = S3_FILE_SYSTEM.getPath("a");

        assertThat(path.getParent()).isNull();
    }

    @Test
    public void ensureGetNameCountReturnsZeroForRootPath() {
        Path path = S3_FILE_SYSTEM.getPath("/");

        assertThat(path.getNameCount()).isEqualTo(0);
    }

    @Test
    public void ensureGetNameCountReturnsOneForEmptyPath() {
        Path path = S3_FILE_SYSTEM.getPath("");

        assertThat(path.getNameCount()).isEqualTo(1);
    }

    @Test
    public void ensureGetNameCountReturnsNumberOfNameElementsForAbsolutePath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.getNameCount()).isEqualTo(3);
    }

    @Test
    public void ensureGetNameCountReturnsNumberOfNameElementsForAbsolutePathWithTrailingSeparator() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c/");

        assertThat(path.getNameCount()).isEqualTo(3);
    }

    @Test
    public void ensureGetNameCountReturnsNumberOfNameElementsForRelativePath() {
        Path path = S3_FILE_SYSTEM.getPath("a/b/c");

        assertThat(path.getNameCount()).isEqualTo(3);
    }

    @Test
    public void ensureGetNameCountReturnsNumberOfNameElementsForRelativePathWithTrailingSeparator() {
        Path path = S3_FILE_SYSTEM.getPath("a/b/c/");

        assertThat(path.getNameCount()).isEqualTo(3);
    }

    @Test
    public void ensureGetNameReturnsNameElementOfSpecificIndex() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.getName(1)).isEqualTo(new S3Path(S3_FILE_SYSTEM, "b"));
    }

    @Test
    public void ensureGetNameReturnsTheClosestNameElementToRoot() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.getName(0)).isEqualTo(new S3Path(S3_FILE_SYSTEM, "a"));
    }

    @Test
    public void ensureGetNameReturnsTheFarthestNameElementFromRoot() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.getName(2)).isEqualTo(new S3Path(S3_FILE_SYSTEM, "c"));
    }

    @Test
    public void ensureGetNameReturnsRightMostElementForRelativePath() {
        Path path = S3_FILE_SYSTEM.getPath("a/b/c");

        assertThat(path.getName(2)).isEqualTo(new S3Path(S3_FILE_SYSTEM, "c"));
    }

    @Test
    public void ensureGetNameThrowsExceptionOnNegativeIndex() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a");
            path.getName(-1);
        });
    }

    @Test
    public void ensureGetNameThrowsExceptionOnIndexOutOfBounds() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a");
            path.getName(1);
        });
    }

    @Test
    public void ensureGetNameThrowsExceptionOnZeroElements() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/");
            path.getName(0);
        });
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
     *
     *                                      @Override
     *     public Path subpath(int beginIndex, int endIndex) {
     *
     *     }
     */

    @Test
    public void ensureSubpathReturnsTheRelativePathFromSpecificBeginAndEndIndicies() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c/d");

        assertThat(path.subpath(1, 4)).isEqualTo(new S3Path(S3_FILE_SYSTEM, "b/c/d"));
    }

    @Test
    public void ensureSubpathThrowsExceptionOnNegativeBeginIndex() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a");
            path.subpath(-1, 1);
        });
    }

    @Test
    public void ensureSubpathThrowsExceptionOnNegativeEndIndex() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a");
            path.subpath(0, -1);
        });
    }

    @Test
    public void ensureSubpathThrowsExceptionOnIndexOutOfBoundsForBeginIndex() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a");
            path.subpath(1, 2);
        });
    }

    @Test
    public void ensureSubpathThrowsExceptionOnIndexOutOfBoundsForEndIndexGreaterThanNumberOfNameElements() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a");
            path.subpath(0, 2);
        });
    }

    @Test
    public void ensureSubpathThrowsExceptionOnIndexOutOfBoundsForEndIndexLessThanBeginIndex() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a/b/c");
            path.subpath(2, 1);
        });
    }

    @Test
    public void ensureSubpathThrowsExceptionOnZeroElements() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/");
            path.subpath(0, 1);
        });
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
     *
     *     @Override
     *     public boolean startsWith(Path other) {
     *         return path.startsWith(other.toString());
     *     }
     */

    @Test
    public void ensureStartsWithReturnsTrueForSameRootAndNameElements() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.startsWith("/a/b/c")).isTrue();
    }

    @Test
    public void ensureStartsWithReturnsFalseIfNumberOfNameElementsDiffer() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.startsWith("/b/c")).isFalse();
    }

    @Test
    public void ensureStartsWithReturnsFalseIfPathRootDoesNotMatch() {
        Path path = S3_FILE_SYSTEM.getPath("a/b/c");

        assertThat(path.startsWith("/a/b/c")).isFalse();
    }

    @Test
    public void ensureStartsWithReturnsTrueWhenNumberOfNameElementsDifferAndTheStartingNameElementsAreTheSameForRoot() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c/d/e");

        assertThat(path.startsWith("/a/b/c")).isTrue();
    }

    @Test
    public void ensureStartsWithReturnsTrueWhenNumberOfNameNameElementsDifferAndTheStartingNameElementsAreTheSameForRelativePath() {
        Path path = S3_FILE_SYSTEM.getPath("a/b/c/d/e");

        assertThat(path.startsWith("a/b/c")).isTrue();
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
     *
     *     @Override
     *     public boolean endsWith(Path other) {
     *         return path.endsWith(other.toString());
     *     }
     */

    @Test
    public void ensureEndsWithReturnsTrueForSameRootAndNameElements() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.endsWith("/a/b/c")).isTrue();
    }

    @Test
    public void ensureEndsWithReturnsFalseIfTheLastNameElementDoesNotMatch() {
        Path path = S3_FILE_SYSTEM.getPath("a/b/c");

        assertThat(path.endsWith("a/b")).isFalse();
    }

    @Test
    public void ensureEndsWithReturnsTrueWhenNumberOfNameElementsDifferAndTheEndingNameElementsAreTheSameForRoot() {
        Path path = S3_FILE_SYSTEM.getPath("/e/a/b/c/d");

        assertThat(path.endsWith("/a/b/c/d")).isTrue();
    }

    @Test
    public void ensureEndsWithReturnsTrueWhenNumberOfNameElementsDifferAndTheEndingNameElementsAreTheSameForRelativePath() {
        Path path = S3_FILE_SYSTEM.getPath("f/e/a/b/c");

        assertThat(path.endsWith("a/b/c")).isTrue();
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
     *
     *     @Override
     *     public Path normalize() {
     *         return this;
     *     }
     */

    @Test
    public void ensureNormalizeReturnsThePathGivenIfThereAreNoRedundantNameElements() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.normalize()).isEqualTo(S3_FILE_SYSTEM.getPath("/a/b/c"));
    }

    @Test
    public void ensureNormalizeReturnsAPathWithAllSinglePeriodNameElementsRemoved() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/.");

        assertThat(path.normalize()).isEqualTo(S3_FILE_SYSTEM.getPath("/a/b"));
    }

    @Test
    public void ensureNormalizeReturnsAPathWithAllDoublePeriodNameElementsRemoved() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/..");

        assertThat(path.normalize()).isEqualTo(S3_FILE_SYSTEM.getPath("/a"));
    }

    @Test
    public void ensureNormalizeReturnsAnEmptyPathForARelativePathWithAllRedundantNameElements() {
        Path path = S3_FILE_SYSTEM.getPath("../././.");

        assertThat(path.normalize()).isEqualTo(S3_FILE_SYSTEM.getPath(""));
    }

    @Test
    public void ensureNormalizeReturnsCurrentPathForEmptyPath() {
        Path path = S3_FILE_SYSTEM.getPath("");

        assertThat(path.normalize()).isEqualTo(path);
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
     *
     *     @Override
     *     public Path resolve(Path other) {
     *         return this;
     *     }
     */

    @Test
    public void ensureResolveReturnsResultingPathForAbsolutePath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");
        String other = "d";

        assertThat(path.resolve(other)).isEqualTo("/a/b/c/d");
    }

    @Test
    public void ensureResolveReturnsPathIfOtherIsAnEmptyPath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");
        String other = "";

        assertThat(path.resolve(other)).isEqualTo("/a/b/c");
    }

    @Test
    public void ensureResolveThrowsExceptionWhenOtherIsAnAbsolutePath() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a/b/c");
            String other = "/d";
            path.resolve(other);
        });
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
     *                                      @Override
     *     public Path relativize(Path other) {
     *         return null;
     *     }
     */

    @Test
    public void ensureRelativizeReturnsAnEmptyPathForTheSamePath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");
        Path other = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.relativize(other)).isEqualTo("");
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
     *                               @Override
     *     public URI toUri() {
     *         return null;
     *     }
     */

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
     *                               @Override
     *     public Path toAbsolutePath() {
     *         return this;
     *     }
     */

    @Test
    public void ensureToAbsolutePathReturnsRootPath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.toAbsolutePath()).isEqualTo("/a/b/c");
    }

    @Test
    public void ensureToAbsolutePathReturnsRootPathIfGivenPathIsRelative() {
        Path path = S3_FILE_SYSTEM.getPath("a/b/c");

        assertThat(path.toAbsolutePath()).isEqualTo("/a/b/c");
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
     *                               @Override
     *     public Path toRealPath(LinkOption... options) throws IOException {
     *         return null;
     *     }
     */

    @Test
    public void ensureToRealPathReturnsAnAbsolutePathWhenTheGivenPathIsRelative() throws IOException {
        Path path = S3_FILE_SYSTEM.getPath("a/b/c");

        assertThat(path.toRealPath()).isEqualTo("/a/b/c");
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
     *                                           @Override
     *     public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
     *         throw new UnsupportedOperationException("File watching is not supported by S3 paths.");
     *     }
     */


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
     *
     *
     @Override
     public int compareTo(Path other) {
     S3Path otherS3 = (S3Path) other;
     return path.compareTo(otherS3.toString());
     }
     */
}
