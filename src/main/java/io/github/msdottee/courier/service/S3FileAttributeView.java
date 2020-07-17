package io.github.msdottee.courier.service;

import java.nio.file.attribute.FileAttributeView;

public class S3FileAttributeView implements FileAttributeView {

    /**
     * Returns the name of the attribute view.
     *
     * @return the name of the attribute view
     */
    @Override
    public String name() {
        return getClass().getSimpleName();
    }
}
