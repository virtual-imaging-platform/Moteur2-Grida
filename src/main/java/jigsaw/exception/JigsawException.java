package jigsaw.exception;

/**
 * Created by abonnet on 1/30/19.
 *
 * package and class must not change, that overides an existing class used in moteur
 *
 */
public class JigsawException extends Exception {
    public JigsawException(String message) {
        super(message);
    }

    public JigsawException(String message, Throwable cause) {
        super(message, cause);
    }
}
