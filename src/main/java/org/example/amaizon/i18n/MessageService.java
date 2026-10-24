package org.example.amaizon.i18n;


import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageService {
    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String keyMessage, Locale locale, Object... args) {
        return messageSource.getMessage(keyMessage, args, locale);
    }

}
