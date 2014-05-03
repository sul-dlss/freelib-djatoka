
package info.freelibrary.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.XMLBundleControl;
import info.freelibrary.util.XMLResourceBundle;

public abstract class AbstractPairtreeMojo extends AbstractMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPairtreeMojo.class);

    protected static final String PAIRTREE_FS = "djatoka.jp2.data";

    protected static final String PAIRTREE_CACHE = "djatoka.view.cache";

    protected final XMLResourceBundle BUNDLE = (XMLResourceBundle) ResourceBundle.getBundle(
            "freelib-djatoka_messages", new XMLBundleControl());

    /**
     * The Maven project directory.
     */
    @Component
    protected MavenProject myProject;

    protected void deletePairtreeJP2Cache(final String aID) throws MojoExecutionException {
        final String jp2s = myProject.getProperties().getProperty(PAIRTREE_FS);

        try {
            final PairtreeRoot ptRoot = new PairtreeRoot(new File(jp2s));

            if (aID != null && !aID.equals("all")) {
                final PairtreeObject ptObj = ptRoot.getObject(aID);

                if (ptObj.exists()) {
                    if (!FileUtils.delete(ptObj)) {
                        LOGGER.error(BUNDLE.get("PT_TREE_DELETE", ptObj));
                    }
                }
            } else {
                if (ptRoot.exists()) {
                    if (FileUtils.delete(ptRoot)) {
                        ptRoot.mkdirs();
                    } else if (LOGGER.isErrorEnabled()) {
                        LOGGER.error(BUNDLE.get("PT_TREE_DELETE", ptRoot));
                    }
                }
            }
        } catch (final FileNotFoundException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        } catch (final IOException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        }
    }

    protected void deletePairtreeImageCache(final String aID) throws MojoExecutionException {
        final String cache = myProject.getProperties().getProperty(PAIRTREE_CACHE);

        try {
            final PairtreeRoot ptCache = new PairtreeRoot(new File(cache));

            if (aID != null && !aID.equals("all")) {
                final PairtreeObject cacheObj = ptCache.getObject(aID);

                if (cacheObj.exists()) {
                    if (!FileUtils.delete(cacheObj)) {
                        LOGGER.error(BUNDLE.get("PT_CACHE_DELETE", cacheObj));
                    }
                }
            } else {
                if (ptCache.exists()) {
                    if (FileUtils.delete(ptCache)) {
                        ptCache.mkdirs();
                    } else if (LOGGER.isErrorEnabled()) {
                        LOGGER.error(BUNDLE.get("PT_CACHE_DELETE", ptCache));
                    }
                }
            }
        } catch (final FileNotFoundException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        } catch (final IOException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        }
    }
}
