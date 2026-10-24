package org.example.amaizon.model.embeddable.all;

import jakarta.persistence.Embeddable;
import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextFactory;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.function.BiConsumer;

@Embeddable
public class Brand extends AbstractTextValue {

    private String value;

    private Brand(String brand){
        super(brand);
    }

    protected Brand(){}

    public static Brand of(String raw, TextSpec spec, ValidationKeys<TextValueObjectValidationException.Reason> keys, BiConsumer<String, Object[]> throwWithKey){
        return TextFactory.createOrThrow(
                raw,
                spec,
                keys,
                throwWithKey,
                null,
                Brand::new
        );
    }

}
