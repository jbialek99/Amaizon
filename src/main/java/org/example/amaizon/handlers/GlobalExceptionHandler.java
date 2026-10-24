package org.example.amaizon.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.amaizon.exceptions.EntityNotFoundException;
import org.example.amaizon.exceptions.KeyedRuntimeException;
import org.example.amaizon.exceptions.UnknownMarketException;
import org.example.amaizon.i18n.MessageService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    public static final String FORM_VIEW_ATTR = "amaizon.formView";

    private final MessageService messageService;

    public GlobalExceptionHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @ExceptionHandler(UnknownMarketException.class)
    public String unsupportedMarketHandler(UnknownMarketException ex,
                                           HttpServletResponse response,
                                           Model model) {
        var locale = LocaleContextHolder.getLocale();
        String message = messageService.getMessage(ex.getKeyMessage(), locale, ex.getArgs());

        response.setStatus(ex.getStatus().value());
        model.addAttribute("marketError", message);
        model.addAttribute("status", ex.getStatus().value());
        return "error/unsupported-market";
    }

    @ExceptionHandler(KeyedRuntimeException.class)
    public String keyedValidationHandler(KeyedRuntimeException ex,
                                         HttpServletRequest request,
                                         Model model) {

        var locale = LocaleContextHolder.getLocale();
        String messageKey = ex.getKeyMessage();
        Object[] args = ex.getArgs();
        String message = messageService.getMessage(messageKey, locale, args);


        Object form = model.asMap().get("form");
        if (form == null) {
            form = request.getAttribute("form");
            if (form != null) model.addAttribute("form", form);
        }


        BindingResult br = extractBindingResult(model);
        if (br == null && form != null) {
            br = new BeanPropertyBindingResult(form, "form");
            model.addAttribute("org.springframework.validation.BindingResult.form", br);
        }


        if (br != null) {
            if (ex.getField() != null && !ex.getField().isBlank()) {
                br.rejectValue(ex.getField(), messageKey, args, message);
            } else {
                br.reject(messageKey, args, message);
            }
        } else {
            model.addAttribute("validationError", message);
        }

        String formView = (String) request.getAttribute(FORM_VIEW_ATTR);
        if (formView != null && !formView.isBlank()) {
            return formView;
        }

        return "error/validation";
    }

    private BindingResult extractBindingResult(Model model) {
        Object maybe = model.asMap().get("org.springframework.validation.BindingResult.form");
        if (maybe instanceof BindingResult br) return br;

        for (var entry : model.asMap().entrySet()) {
            if (entry.getKey() != null &&
                    entry.getKey().startsWith("org.springframework.validation.BindingResult.") &&
                    entry.getValue() instanceof BindingResult br2) {
                return br2;
            }
        }
        return null;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFound(EntityNotFoundException ex,
                                       HttpServletResponse response,
                                       Model model) {
        var locale = LocaleContextHolder.getLocale();
        HttpStatus status = ex.getStatus();
        String message = messageService.getMessage(ex.getMessageKey(), locale, ex.getArgs());
        response.setStatus(ex.getStatus().value());
        model.addAttribute("entityError", message);
        model.addAttribute("status", status);
        return "entity-not-found";
    }
}
