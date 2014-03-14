
package info.freelibrary.djatoka.iiif;

/**
 * An exception throw from FreeLib-Djatoka's IIIF interface.
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class IIIFException extends Exception {

    private static final long serialVersionUID = -5457634583786308472L;

    /**
     * Creates a new <code>IIIFException</code>.
     */
    public IIIFException() {
        super();
    }

    /**
     * Creates a new <code>IIIFException</code> using the supplied exception message.
     * 
     * @param aMessage The detailed exception message
     */
    public IIIFException(String aMessage) {
        super(aMessage);
    }

    /**
     * Creates a new <code>IIIFException</code> with the supplied exception as the cause.
     * 
     * @param aException The exception that was the cause of this exception
     */
    public IIIFException(Exception aException) {
        super(aException);
    }

    /**
     * Creates a new <code>IIIFException</code> with the supplied detailed exception message and parent exception.
     * 
     * @param aMessage The detailed exception message
     * @param aException The exception that was the cause of this exception
     */
    public IIIFException(String aMessage, Exception aException) {
        super(aMessage, aException);
    }

}
