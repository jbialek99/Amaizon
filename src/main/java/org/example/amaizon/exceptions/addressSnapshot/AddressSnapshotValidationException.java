package org.example.amaizon.exceptions.addressSnapshot;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class AddressSnapshotValidationException extends KeyedRuntimeException {
    public AddressSnapshotValidationException(String message, Object...args) {
        super(message, args);
    }
}
