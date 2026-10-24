package org.example.amaizon.model.validators.imageUrl;

import java.util.Objects;
import java.util.Set;

public record ImageUrlSpec(

        int minLength,
        int maxLength,

        boolean requireHttps,
        boolean allowQuery,
        boolean allowFragment,
        boolean requireExactHost,

        Set<String> allowedHosts,
        Set<String> allowedExtensions

) {

    public ImageUrlSpec {

        if (minLength < 0)
            throw new IllegalArgumentException("minLength must be >= 0");

        if (maxLength <= 0)
            throw new IllegalArgumentException("maxLength must be > 0");

        if (minLength > maxLength)
            throw new IllegalArgumentException("minLength cannot be greater than maxLength");

        Objects.requireNonNull(allowedHosts, "allowedHosts cannot be null");
        Objects.requireNonNull(allowedExtensions, "allowedExtensions cannot be null");

        if (allowedHosts.isEmpty())
            throw new IllegalArgumentException("allowedHosts cannot be empty");

        if (allowedExtensions.isEmpty())
            throw new IllegalArgumentException("allowedExtensions cannot be empty");

        allowedHosts = Set.copyOf(allowedHosts);
        allowedExtensions = Set.copyOf(allowedExtensions);
    }



    public static ImageUrlSpec httpsExactHost(
            int minLength,
            int maxLength,
            Set<String> hosts,
            Set<String> extensions
    ) {
        return new ImageUrlSpec(
                minLength,
                maxLength,
                true,
                false,
                false,
                true,
                hosts,
                extensions
        );
    }

    public static ImageUrlSpec flexible(
            int minLength,
            int maxLength,
            Set<String> hosts,
            Set<String> extensions
    ) {
        return new ImageUrlSpec(
                minLength,
                maxLength,
                true,
                true,
                false,
                false,
                hosts,
                extensions
        );
    }
}
