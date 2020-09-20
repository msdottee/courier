package io.github.msdottee.courier.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

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

    @Test
    public void ensureSubpathReturnsTheRelativePathFromSpecificBeginAndEndIndicies() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c/d");

        assertThat(path.subpath(1, 4)).isEqualTo(new S3Path(S3_FILE_SYSTEM, "b/c/d"));
    }

    @Test
    public void ensureSubpathThrowsIllegalArgumentExceptionForEmptyPath() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("");
            path.subpath(0, 0);
        });
    }

    @Test
    public void ensureSubpathThrowsIllegalArgumentExceptionOnNegativeBeginIndex() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a");
            path.subpath(-1, 1);
        });
    }

    @Test
    public void ensureSubpathThrowsIllegalArgumentExceptionOnNegativeEndIndex() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a");
            path.subpath(0, -1);
        });
    }

    @Test
    public void ensureSubpathThrowsIllegalArugmentExceptionOnIndexOutOfBoundsForBeginIndexGreaterThanNumberOfNameElements() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a");
            path.subpath(3, 2);
        });
    }

    @Test
    public void ensureSubpathThrowsIllegalArgumentExceptionOnIndexOutOfBoundsForEndIndexGreaterThanNumberOfNameElements() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a");
            path.subpath(0, 2);
        });
    }

    @Test
    public void ensureSubpathThrowsIllegalArgumentExceptionOnIndexOutOfBoundsForEndIndexLessThanBeginIndex() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a/b/c");
            path.subpath(2, 1);
        });
    }

    @Test
    public void ensureSubpathThrowsIllegalArgumentExceptionForRootPath() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/");
            path.subpath(0, 1);
        });
    }

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

    @Test
    public void ensureStartsWithReturnsFalseForNonS3Path() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.startsWith(Paths.get("/a/b"))).isFalse();
    }

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

    @Test
    public void ensureEndsWithReturnsFalseForNonS3Path() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.endsWith(Paths.get("/a/b/c"))).isFalse();
    }

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

    @Test
    public void ensureResolveReturnsResultingPathForAbsolutePath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.resolve(S3_FILE_SYSTEM.getPath("d"))).isEqualTo(S3_FILE_SYSTEM.getPath("/a/b/c/d"));
    }

    @Test
    public void ensureResolveReturnsPathIfOtherIsAnEmptyPath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.resolve(S3_FILE_SYSTEM.getPath(""))).isEqualTo(S3_FILE_SYSTEM.getPath("/a/b/c"));
    }

    @Test
    public void ensureResolveReturnsOtherWhenOtherIsAnAbsolutePath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.resolve(S3_FILE_SYSTEM.getPath("/d"))).isEqualTo(S3_FILE_SYSTEM.getPath("/d"));
    }

    @Test
    public void ensureResolveThrowsNullPointerExceptionWhenOtherIsNull() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> {
            S3_FILE_SYSTEM.getPath("/a/b/c").resolve((Path) null);
        });
    }

    @Test
    public void ensureResolveThrowsIllegalArgumentExceptionWhenOtherIsNotS3Path() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            S3_FILE_SYSTEM.getPath("/a/b/c").resolve(Paths.get("/"));
        });
    }

    @Test
    public void ensureRelativizeReturnsAnEmptyPathForTheSamePath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.relativize(S3_FILE_SYSTEM.getPath("/a/b/c"))).isEqualTo(S3_FILE_SYSTEM.getPath(""));
    }

    @Test
    public void ensureRelativizeReturnsARelativePathWhenTheRootIsTheSame() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b");

        assertThat(path.relativize(S3_FILE_SYSTEM.getPath("/a/b/c/d"))).isEqualTo(S3_FILE_SYSTEM.getPath("c/d"));
    }

    @Test
    public void ensureRelativizeThrowsIllegalArgumentExceptionWhenCurrentPathIsRoot() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            S3_FILE_SYSTEM.getPath("/a/b/c").relativize(S3_FILE_SYSTEM.getPath("b/c"));
        });
    }

    @Test
    public void ensureRelativizeThrowsIllegalArgumentExceptionWhenCurrentPathIsRelative() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            S3_FILE_SYSTEM.getPath("a/b/c").relativize(S3_FILE_SYSTEM.getPath("/a/b/c"));
        });
    }

    @Test
    public void ensureRelativizeThrowsIllegalArgumentExceptionWhenOtherIsNotS3Path() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            S3_FILE_SYSTEM.getPath("/a/b/c").relativize(Paths.get("/b/c"));
        });
    }

    @Test
    public void ensureRelativizeThrowsNullPointerExceptionWhenOtherIsNull() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> {
            S3_FILE_SYSTEM.getPath("/a/b/c").relativize((Path) null);
        });
    }

    @Test
    public void ensureToUriReturnsUriForAbsolutePath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.toUri().toString()).isEqualTo("s3://a/b/c");
    }

    @Test
    public void ensureToUriReturnsUriForRelativePath() {
        Path path = S3_FILE_SYSTEM.getPath("a/b/c");

        assertThat(path.toUri().toString()).isEqualTo("s3://a/b/c");
    }

    @Test
    public void ensureToAbsolutePathReturnsRootPath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.toAbsolutePath()).isEqualTo(S3_FILE_SYSTEM.getPath("/a/b/c"));
    }

    @Test
    public void ensureToAbsolutePathReturnsRootPathIfGivenPathIsRelative() {
        Path path = S3_FILE_SYSTEM.getPath("a/b/c");

        assertThat(path.toAbsolutePath()).isEqualTo(S3_FILE_SYSTEM.getPath("/a/b/c"));
    }

    @Test
    public void ensureToRealPathThrowsUnsupportedOperationException() throws IOException {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> {
            Path path = S3_FILE_SYSTEM.getPath("/a");

            assertThat(path.toRealPath()).isEqualTo(S3_FILE_SYSTEM.getPath("/a/b/d"));
        });
    }

    @Test
    public void ensureRegisterThrowsUnsupportedException() throws IOException {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> {
            S3_FILE_SYSTEM.getPath("/a").register(new WatchService() {
                @Override
                public void close() throws IOException {

                }

                @Override
                public WatchKey poll() {
                    return null;
                }

                @Override
                public WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
                    return null;
                }

                @Override
                public WatchKey take() throws InterruptedException {
                    return null;
                }
            }, StandardWatchEventKinds.ENTRY_CREATE);
        });
    }

    @Test
    public void ensureGetS3KeyReturnsPathWithoutRoot() {
        Path path = S3_FILE_SYSTEM.getPath("/a");

        assertThat(((S3Path) path).getS3Key()).isEqualTo("a");
    }

    @Test
    public void ensureGetS3KeyNormalizesPath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/../b");

        assertThat(((S3Path) path).getS3Key()).isEqualTo("b");

    }

    @Test
    public void ensureGetS3KeyConvertsRelativePath() {
        Path path = S3_FILE_SYSTEM.getPath("a/b");

        assertThat(((S3Path) path).getS3Key()).isEqualTo("a/b");
    }

    @Test
    public void ensureCompareToReturnsZeroWhenBothPathsAreEqual() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.compareTo(S3_FILE_SYSTEM.getPath("/a/b/c"))).isEqualTo(0);
    }

    @Test
    public void ensureCompareToReturnsANegativeValueForPathLexicographicallyLess() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b");

        assertThat(path.compareTo(S3_FILE_SYSTEM.getPath("/a/b/c/d"))).isLessThan(0);
    }

    @Test
    public void ensureCompareToReturnsAPositiveValueForPathLexicographicallyGreater() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c/d");

        assertThat(path.compareTo(S3_FILE_SYSTEM.getPath("/a/b"))).isGreaterThan(0);
    }

    @Test
    public void ensureCompareToThrowsNullPointerExceptionWhenOtherIsNull() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> {
            S3_FILE_SYSTEM.getPath("/a/b/c").compareTo((Path) null);
        });
    }

    @Test
    public void ensureToStringReturnsPath() {
        Path path = S3_FILE_SYSTEM.getPath("/a/b/c");

        assertThat(path.toString()).isEqualTo("/a/b/c");
    }

    @Test
    public void ensureToS3PrefixReturnsPrefixForAbsolutePath() {
        S3Path s3Path = (S3Path) S3_FILE_SYSTEM.getPath("/a/b");

        assertThat(s3Path.toS3Prefix()).isEqualTo("a/b/");
    }

    @Test
    public void ensureToS3PrefixReturnsPrefixForRelativePath() {
        S3Path s3Path = (S3Path) S3_FILE_SYSTEM.getPath("a/b");

        assertThat(s3Path.toS3Prefix()).isEqualTo("a/b/");
    }

    @Test
    public void ensureToS3PrefixReturnsEmptyStringForRootPath() {
        S3Path s3Path = (S3Path) S3_FILE_SYSTEM.getPath("/");

        assertThat(s3Path.toS3Prefix()).isEqualTo("");
    }

    @Test
    public void ensureToS3PrefixNormalizesThePath() {
        S3Path s3Path = (S3Path) S3_FILE_SYSTEM.getPath("/a/../b");

        assertThat(s3Path.toS3Prefix()).isEqualTo("b/");
    }
}
