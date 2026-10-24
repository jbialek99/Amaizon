package org.example.amaizon.model.validators;

import java.util.Map;

public final class ValidationKeys<R extends Enum<R>> {

    private final Map<R, String> keys;

    public ValidationKeys(Map<R, String> keys) {
        this.keys = Map.copyOf(keys);
    }

    public String resolve(R reason) {
        String key = keys.get(reason);

        if (key == null) {
            throw new IllegalStateException(
                    "Missing key mapping for reason: " + reason
            );
        }
        return key;
    }


}
