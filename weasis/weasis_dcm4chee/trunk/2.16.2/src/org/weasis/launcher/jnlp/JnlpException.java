package org.weasis.launcher.jnlp;

/**
 * This class implements exceptions raised by the classes present in the jnlp package
 * 
 * @author jlrz
 */
public class JnlpException extends Throwable {

    private static final long serialVersionUID = 1L;

    // Instance attribute
    private String message = "";
    private int exception = NO_EXCEPTION;

    // Predefined exceptions codes
    public static int NO_EXCEPTION = 0;
    public static int CANNOT_CREATE_JNLP_FILE = 1;
    public static int CANNOT_WRITE_TO_JNLP_FILE = 2;

    // Predefined exceptions messages
    private static String exceptions[] = { "No Exception", "Cannot Create Jnlp File", "Cannot Write To Jnlp File" };

    // Constructors
    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param msg
     *            a detail message
     */
    public JnlpException(String msg) {
        message = msg;
    }

    /**
     * Constructs a new exception with a predefined exception
     * 
     * @param exception
     *            a predefined exception code
     */
    public JnlpException(int code) {
        exception = code;
        message = exceptions[code];
    }

    // Methods
    /**
     * Returns a short description of this exception
     * 
     * @return a string representation of this exception
     */
    @Override
    public String toString() {
        return new String("JnlpException: " + message);
    }

    /**
     * Returns the detail message string of this throwable
     * 
     * @return the detail message string of this throwable
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Returns the code of this exception
     * 
     * @return the code of this exception
     */
    public int getExceptionCode() {
        return exception;
    }

}
