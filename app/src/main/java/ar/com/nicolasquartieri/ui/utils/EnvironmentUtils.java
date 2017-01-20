package ar.com.nicolasquartieri.ui.utils;

import ar.com.nicolasquartieri.BuildConfig;

/**
 * Utility to get configuration according build environment.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class EnvironmentUtils {
    /** Local dev environment. */
    public static final String DEV = "dev";
    /** QA environment. */
    public static final String QA = "qa";
    /** Staging environment. */
    public static final String STAGING = "staging";
    /** Production environment. */
    public static final String PRODUCTION = "production";
    /** Flickr API KEY */
    private static final String API_KEY = "67694921845e1e630e1be511d82a6f53";

    /**
     * Returns the base url according current build environment.
     * @return the base url, representing the core service.
     */
    public static String getBaseUrl() {
        switch (BuildConfig.FLAVOR) {
            case EnvironmentUtils.PRODUCTION:
                return "https://api.flickr.com/services/rest/";
            case EnvironmentUtils.STAGING:
                return "https://api.flickr.com/services/rest/";
            case EnvironmentUtils.QA:
                return "https://api.flickr.com/services/rest/";
            case EnvironmentUtils.DEV:
                return "https://api.flickr.com/services/rest/";
            default:
                throw new AssertionError("Invalid environment: " + BuildConfig.FLAVOR);
        }
    }

    /** This class cannot be instantiated. */
    private EnvironmentUtils() {
    }

    /** Get the Flickr API KEY */
    public static String getApiKey() {
        return API_KEY;
    }
}
