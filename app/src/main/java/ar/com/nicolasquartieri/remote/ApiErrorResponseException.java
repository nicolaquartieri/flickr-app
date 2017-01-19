package ar.com.nicolasquartieri.remote;

import android.content.Context;
import android.os.Bundle;

/**
 * Exception that is meant to be thrown when an error is found
 * during {@link ApiService#execute(Context, Bundle)} and the
 * service is executing in synchronous mode.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class ApiErrorResponseException extends RuntimeException {

    /** Information that describes the error, never null. */
    private final ApiErrorResponse mApiErrorResponse;

    /**
     * Default constructor with mandatory parameters.
     *
     * @param apiErrorResponse Information that describes the error that
     * triggers this exception, cannot be null.
     */
    public ApiErrorResponseException(final ApiErrorResponse apiErrorResponse) {
        mApiErrorResponse = apiErrorResponse;
    }

    /**
     * Retrieves the information that describes the error based on which
     * the exception was triggered.
     *
     * @return The error information, never null
     */
    public ApiErrorResponse getApiErrorResponse() {
        return mApiErrorResponse;
    }
}
