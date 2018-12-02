package com.kellnhofer.tracker.util;

public class ValidationUtils {

    private static final String REGEX_SCHEME = "^(http|https)$";
    private static final String REGEX_DOMAIN = "^([a-z0-9\\-\\_\\~]+\\.)+[a-z]+$";
    private static final String REGEX_IP = "^([1-9]|[1-9]\\d|[1]\\d\\d|2[0-4]\\d|25[0-4])\\." +
                                           "([1-9]|[1-9]\\d|[1]\\d\\d|2[0-4]\\d|25[0-4])\\." +
                                           "([1-9]|[1-9]\\d|[1]\\d\\d|2[0-4]\\d|25[0-4])\\." +
                                           "([1-9]|[1-9]\\d|[1]\\d\\d|2[0-4]\\d|25[0-4])$";
    private static final String REGEX_PORT = "^\\d|[1-9][0-9]+$";
    private static final String REGEX_PATH = "^(\\/[a-z0-9\\-\\_\\~]+)*$";

    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;

    private ValidationUtils() {

    }

    public static boolean checkIsValidServerUrl(String url) {
        String[] urlParts = splitUrl(url);

        return isValidScheme(urlParts[0]) &&
                isValidDomain(urlParts[1]) &&
                isValidPort(urlParts[2]) &&
                isValidPath(urlParts[3]);
    }

    private static String[] splitUrl(String url) {
        if (url == null) {
            return new String[]{null, null, null, null};
        }

        String[] p1 = url.split(":\\/\\/", 2);
        String scheme = p1[0];
        if (p1.length <= 1) {
            return new String[]{scheme, null, null, null};
        }

        String[] p2 = p1[1].split("\\/", 2);
        String[] p3 = p2[0].split("\\:", 2);
        String domain = p3[0];
        String port = p3.length > 1 ? p3[1] : null;
        String path = p2.length > 1 ? p2[1] : null;

        return new String[]{scheme, domain, port, path};
    }

    private static boolean isValidScheme(String scheme) {
        return scheme != null && scheme.matches(REGEX_SCHEME);
    }

    private static boolean isValidDomain(String domain) {
        return domain != null && (domain.matches(REGEX_DOMAIN) || domain.matches(REGEX_IP));
    }

    private static boolean isValidPort(String port) {
        if (port == null) {
            return true;
        }

        if (!port.matches(REGEX_PORT)) {
            return false;
        }

        int p;
        try {
            p = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return false;
        }

        return p >= MIN_PORT && p <= MAX_PORT;
    }

    private static boolean isValidPath(String path) {
        if (path == null) {
            return true;
        }

        path = "/" + path;
        return path.matches(REGEX_PATH);
    }

}
