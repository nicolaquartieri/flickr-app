package ar.com.nicolasquartieri.ui.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Utility class to manage the keyboard.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public final class KeyboardUtils {

	private KeyboardUtils() {
		throw new AssertionError("Util class must not be instantiated.");
	}

	/**
	 * Request to hide the keyboard from the context of the view that is currently accepting input.
	 *
	 * @param context The context
	 * @param view The view that will be used to hide the keyboard.
	 */
	public static void hideSoftKeyboard(@NonNull final Context context, @NonNull final View view) {
		// Hiding keyboard
		final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
}