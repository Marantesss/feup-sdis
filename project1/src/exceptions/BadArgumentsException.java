package exceptions;

public class BadArgumentsException extends Exception {

    /**
     * Serial version universal identifier
     */
    private static final long serialVersionUID = 5392118616568741466L;

    /**
     * Constructor
     * 
     * @param errorMessage Exception error message
     */
    public BadArgumentsException(String errorMessage) {
        super(errorMessage);
    }
}