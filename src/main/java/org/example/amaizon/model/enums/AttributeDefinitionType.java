package org.example.amaizon.model.enums;

public enum AttributeDefinitionType {
    TEXT,       // valueString
    INT,        // valueNumber (scale = 0)
    DECIMAL,    // valueNumber
    BOOLEAN,    // valueBoolean
    TIMESTAMP,  // valueTimestamp (Instant)
    CODE        // valueCode (option code / dictionary)
}
