package ui.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Custom Gson object for Deserializer & Serializer.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class GsonHelper {
    /** Gson custom object */
    private static Gson gson;

    /**
     * Get the custom Gson if not create one.
     * @return The {@link Gson}.
     */
    public static synchronized Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder().create();
        }
        return gson;
    }
}
