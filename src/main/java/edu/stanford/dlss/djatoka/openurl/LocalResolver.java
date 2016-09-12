package edu.stanford.dlss.djatoka.openurl;

import gov.lanl.adore.djatoka.openurl.IReferentMigrator;
import gov.lanl.adore.djatoka.openurl.IReferentResolver;
import gov.lanl.adore.djatoka.openurl.ResolverException;
import gov.lanl.adore.djatoka.util.ImageRecord;
import info.openurl.oom.entities.Referent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Properties;
import javax.servlet.http.HttpServletResponse;

 /**
 * A referent resolver that expects all image uris to be file:// and all images to be local. It skips the image migration step, so you arent copying from local disk to cache, because that is a waste of time
 * @author jdeering
 */
public class LocalResolver implements IReferentResolver {
     private static final Logger LOGGER = LoggerFactory.getLogger(LocalResolver.class);

    @Override
    public ImageRecord getImageRecord(final String aRequest) throws ResolverException {
        //I was getting newlines in the uris for an unknown reason, they get removed here.
        final String decodedRequest = decode(aRequest);

//        if (LOGGER.isDebugEnabled()) {
            LOGGER.error("Found a locally resolvable ID: {}", decodedRequest);
//        }

        String id=decodedRequest.replace("\n","");
        String rft=id.replace("file://", "");
        return new ImageRecord(id,rft);
    }

    @Override
    public ImageRecord getImageRecord(final Referent aReferent) {
        final String id = ((URI) aReferent.getDescriptors()[0]).toASCIIString();
        final String decodedRequest = decode(id);
        return new ImageRecord(decodedRequest,decodedRequest.replace("file://", ""));
    }

    @Override
    public void setProperties(Properties props) throws ResolverException {

    }

    @Override
    public int getStatus(final String aRequest) {
        final String decodedRequest = decode(aRequest);
//        if (LOGGER.isDebugEnabled()) {
            LOGGER.error("Found a locally resolvable ID: {}", decodedRequest);
//        }


        //I hope this speeds up head requests significantly
        File target=new File(decodedRequest.replace("file://", ""));
        if (target.exists()) {
            return HttpServletResponse.SC_OK;
        } else {

//            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("But the file did not exist: {}", decodedRequest);
//            }
            return HttpServletResponse.SC_NOT_FOUND;
        }

    }

    @Override
    public IReferentMigrator getReferentMigrator() {
        return null;
    }

     private String decode(final String aRequest) {
         try {
             final String request = URLDecoder.decode(aRequest, "UTF-8");
             return URLDecoder.decode(request, "UTF-8");
         } catch (final UnsupportedEncodingException details) {
             throw new RuntimeException("JVM doesn't support UTF-8!!", details);
         }
     }

}