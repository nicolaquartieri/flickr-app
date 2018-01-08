package ar.com.nicolasquartieri.remote;

import android.os.Bundle;

/**
 * Wrapper for encapsulate the service playload and {@link Bundle} arguments
 *
 * @param <T> The playload generic.
 *
 * @author Nicolas Quartieri (nicolas.quartieri@gmail.com)
 */
public class ResponseType<T> {
    private T playload;
    private Bundle args;

    public ResponseType(T playload, ApiErrorResponse apiErrorResponse) {
        this.playload = playload;
        args = new Bundle();
        args.putParcelable(ApiErrorResponse.EXTRA_RESPONSE_ERROR, apiErrorResponse);
    }

    public T getPlayload() {
        return playload;
    }

    public Bundle getArgs() {
        return args;
    }
}
