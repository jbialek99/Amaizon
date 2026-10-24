package org.example.amaizon.model.validators.imageUrl;

import org.example.amaizon.exceptions.imageUrl.ImageUrlValueObjectValidationException;

import java.net.URI;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.UnaryOperator;

public final class ImageUrlValidator {

    private ImageUrlValidator() {}

    public static String validate(String raw, ImageUrlSpec spec, UnaryOperator<String> normalize) {

        if (raw == null)
            ex(ImageUrlValueObjectValidationException.Reason.NULL);

        String trimmed = raw.trim();

        if (trimmed.isEmpty())
            ex(ImageUrlValueObjectValidationException.Reason.EMPTY);

        UnaryOperator<String> effectiveNormalize =
                (normalize != null) ? normalize : defaultNormalize();

        String normalized = effectiveNormalize.apply(trimmed);

        int len = normalized.length();

        if (len < spec.minLength())
            ex(ImageUrlValueObjectValidationException.Reason.TOO_SHORT, spec.minLength());

        if (len > spec.maxLength())
            ex(ImageUrlValueObjectValidationException.Reason.TOO_LONG, spec.maxLength());

        URI uri;
        try {
            uri = URI.create(normalized);
        } catch (IllegalArgumentException e) {
            ex(ImageUrlValueObjectValidationException.Reason.INVALID_URI);
            return null;
        }

        // scheme
        String scheme = uri.getScheme();
        if (scheme == null)
            ex(ImageUrlValueObjectValidationException.Reason.INVALID_SCHEME,
                    spec.requireHttps() ? "https" : "http/https");

        if (spec.requireHttps() && !"https".equalsIgnoreCase(scheme))
            ex(ImageUrlValueObjectValidationException.Reason.INVALID_SCHEME, "https");

        // host
        String host = uri.getHost();
        if (host == null || host.isBlank())
            ex(ImageUrlValueObjectValidationException.Reason.HOST_NOT_ALLOWED,
                    joinSorted(spec.allowedHosts()));

        if (!isHostAllowed(host, spec))
            ex(ImageUrlValueObjectValidationException.Reason.HOST_NOT_ALLOWED,
                    joinSorted(spec.allowedHosts()));

        // query
        if (!spec.allowQuery() && uri.getRawQuery() != null)
            ex(ImageUrlValueObjectValidationException.Reason.QUERY_NOT_ALLOWED);

        // fragment
        if (!spec.allowFragment() && uri.getRawFragment() != null)
            ex(ImageUrlValueObjectValidationException.Reason.FRAGMENT_NOT_ALLOWED);

        // path
        String path = uri.getPath();
        if (path == null || path.isBlank() || "/".equals(path))
            ex(ImageUrlValueObjectValidationException.Reason.INVALID_PATH);

        int slash = path.lastIndexOf('/');
        String file = (slash >= 0) ? path.substring(slash + 1) : path;

        if (file.isBlank())
            ex(ImageUrlValueObjectValidationException.Reason.INVALID_PATH);

        int dot = file.lastIndexOf('.');
        if (dot <= 0 || dot == file.length() - 1)
            ex(ImageUrlValueObjectValidationException.Reason.INVALID_EXTENSION,
                    joinSorted(spec.allowedExtensions()));

        String ext = file.substring(dot + 1).toLowerCase(Locale.ROOT);

        if (!spec.allowedExtensions().contains(ext))
            ex(ImageUrlValueObjectValidationException.Reason.INVALID_EXTENSION,
                    joinSorted(spec.allowedExtensions()));

        return normalized;
    }


    private static boolean isHostAllowed(String host, ImageUrlSpec spec) {
        if (spec.requireExactHost()) {
            for (String allowed : spec.allowedHosts()) {
                if (host.equalsIgnoreCase(allowed)) return true;
            }
            return false;
        }

        // allow subdomains
        String hostLower = host.toLowerCase(Locale.ROOT);
        for (String allowed : spec.allowedHosts()) {
            String allowedLower = allowed.toLowerCase(Locale.ROOT);
            if (hostLower.equals(allowedLower)) return true;
            if (hostLower.endsWith("." + allowedLower)) return true;
        }
        return false;
    }


    public static UnaryOperator<String> defaultNormalize() {
        return input -> {
            if (input == null) return null;

            String trimmed = input.trim();

            URI uri;
            try {
                uri = URI.create(trimmed);
            } catch (IllegalArgumentException e) {
                return trimmed;
            }

            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null || host == null) {
                return trimmed;
            }

            try {
                URI rebuilt = new URI(
                        scheme.toLowerCase(Locale.ROOT),
                        uri.getUserInfo(),
                        host.toLowerCase(Locale.ROOT),
                        uri.getPort(),
                        uri.getRawPath(),
                        uri.getRawQuery(),
                        uri.getRawFragment()
                );
                return rebuilt.toString();
            } catch (Exception e) {
                return trimmed;
            }
        };
    }

    private static String joinSorted(Set<String> values) {
        return String.join(", ", new TreeSet<>(values));
    }

    private static void ex(ImageUrlValueObjectValidationException.Reason reason, Object... args) {
        throw new ImageUrlValueObjectValidationException(reason, args);
    }
}
