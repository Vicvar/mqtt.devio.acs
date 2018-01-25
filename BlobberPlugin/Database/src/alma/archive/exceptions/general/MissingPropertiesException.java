/**
 * 
 */
package alma.archive.exceptions.general;


/**
 * Where is the archiveConfig.properties file?
 * 
 * @author almadev
 */
public class MissingPropertiesException extends DatabaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public MissingPropertiesException(String message) {
		super(message);
	}

}
