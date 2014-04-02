
package info.freelibrary.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.XMLBundleControl;
import info.freelibrary.util.XMLResourceBundle;

/**
 * Deletes tiles and JP2s from FreeLib-Djatoka's Pairtree file system.
 * <p/>
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
@Mojo(name = "clean-cache")
public class DjatokaCacheMojo extends AbstractMojo {

    private static final String PAIRTREE_FS = "djatoka.jp2.data";

    private static final String PAIRTREE_CACHE = "djatoka.view.cache";

    private static final Logger LOGGER = LoggerFactory.getLogger(DjatokaIngestMojo.class);

    private final XMLResourceBundle BUNDLE = (XMLResourceBundle) ResourceBundle.getBundle("freelib-djatoka_messages",
            new XMLBundleControl());

    /**
     * The Maven project directory.
     */
    @Component
    private MavenProject myProject;

    /**
     * The ID of the item to delete from the pairtree cache.
     */
    @Parameter(property = "id", defaultValue = "all")
    private String myCacheID;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String jp2s = myProject.getProperties().getProperty(PAIRTREE_FS);
        final String cache = myProject.getProperties().getProperty(PAIRTREE_CACHE);

        try {
            final PairtreeRoot ptRoot = new PairtreeRoot(new File(jp2s));
            final PairtreeRoot ptCache = new PairtreeRoot(new File(cache));

            if (!myCacheID.equals("all")) {
                final PairtreeObject ptObj = ptRoot.getObject(myCacheID);
                final PairtreeObject cacheObj = ptCache.getObject(myCacheID);

                if (ptObj.exists()) {
                    if (!FileUtils.delete(ptObj)) {
                        LOGGER.error(BUNDLE.get("PT_TREE_DELETE", ptObj));
                    }
                }

                if (cacheObj.exists()) {
                    if (!FileUtils.delete(cacheObj)) {
                        LOGGER.error(BUNDLE.get("PT_CACHE_DELETE", cacheObj));
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
