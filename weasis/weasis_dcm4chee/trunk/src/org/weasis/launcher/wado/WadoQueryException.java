package org.weasis.launcher.wado;

/**
 * This class implements exceptions raised by the WadoQuery class
 * 
 * @author jlrz
 */
public class WadoQueryException extends Throwable {

    private static final long serialVersionUID = 1L;

    // Instance attribute
    private String message = "";
    private int exception = NO_EXCEPTION;

    // Predefined exceptions codes
    public static int NO_EXCEPTION = 0;
    public static int NO_PATIENTS_LIST = 1;
    public static int BAD_PATH = 2;
    public static int CANNOT_CREATE_TEMP_FILE = 3;
    public static int CANNOT_WRITE_TO_TEMP_FILE = 4;

    // Predefined exceptions messages
    private static String exceptions[] =
        { "No Exception", "No Patients List", "Bad URL Path", "Cannot Create Temporary File",
            "Cannot Write To Temporary File" };

    // Constructors
    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param msg
     *            a detail message
     */
    public WadoQueryException(String msg) {
        message = msg;
    }

    /**
     * Constructs a new exception with a predefined exception
     * 
     * @param exception
     *            a predefined exception code
     */
    public WadoQueryException(int code) {
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
        return new String("WadoQueryException: " + message);
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
