
package info.freelibrary.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes tiles and JP2s from FreeLib-Djatoka's Pairtree file system.
 * <p/>
 *
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
@Mojo(name = "clean-cache")
public class DjatokaCacheMojo extends AbstractPairtreeMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(DjatokaCacheMojo.class);

    /**
     * The ID of the item to delete from the pairtree cache.
     */
    @Parameter(property = "id", defaultValue = "all")
    private String myCacheID;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Sets the Maven loggers' levels (not the levels of loggers used by this plugin)
        MavenUtils.setLogLevels(MavenUtils.ERROR_LOG_LEVEL, MavenUtils.getMavenLoggers());

        // Clear the image caches (JP2 and JPEG)
        deletePairtreeJP2Cache(myCacheID);
        deletePairtreeImageCache(myCacheID);

        if (LOGGER.isInfoEnabled()) {
            if (myCacheID.equals("all")) {
                LOGGER.info("Image cache successfully cleaned");
            } else {
                LOGGER.info("Images for '{}' successfully removed from the cache", myCacheID);
            }
        }
    }

}
