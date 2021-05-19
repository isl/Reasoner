package isl.reasoner;

/** A generic exception thrown when the given extension is not supported.
 *
 * @author Yannis Marketakis (marketak 'at' ics 'dot' forth 'dot' gr)
 */
public class UnsupportedExtensionException extends Exception{

    public UnsupportedExtensionException(String msg){
        super(msg);
    }
}
