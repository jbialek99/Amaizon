package org.example.amaizon.model.validators.intNumber;

import org.example.amaizon.exceptions.intNumber.IntValueObjectValidationException;

public class IntValidator {

    private IntValidator() {}

    public static int validate(Integer raw, IntSpec spec) {

       if(raw == null)
           ex(IntValueObjectValidationException.Reason.NULL);

       int value = raw;

        if(value < spec.min())
            ex(IntValueObjectValidationException.Reason.TOO_SMALL, spec.min());

        if(value > spec.max())
            ex(IntValueObjectValidationException.Reason.TOO_LARGE, spec.max());

        return value;
    }


    private static void ex(IntValueObjectValidationException.Reason reason, Object... args){
        throw new IntValueObjectValidationException(reason,args);
    }


}
