package org.openepics.discs.conf.util;

public class ProtectedDataTypeModificationException extends CCDBRuntimeException {
    private static final long serialVersionUID = -6970090174244668138L;

    /**
     * @param message the message
     * @param cause the cause
     * @see RuntimeException#RuntimeException(String, Throwable)
     */
    public ProtectedDataTypeModificationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message the message
     * @see RuntimeException#RuntimeException(String)
     */
    public ProtectedDataTypeModificationException(String message) {
        super(message);
    }

    /**
     * @param cause the cause
     * @see RuntimeException#RuntimeException(Throwable)
     */
    public ProtectedDataTypeModificationException(Throwable cause) {
        super(cause);
    }

    /**
     * @see RuntimeException#RuntimeException(Throwable)
     */
    public ProtectedDataTypeModificationException() {
        super();
    }


}
