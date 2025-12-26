package ir.service.impl;

import ir.dto.AddressDto;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class AddressFormatter {

    private final MessageSource messageSource;

    public AddressFormatter(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String format(AddressDto a) {
        if (a == null) return "-";

        Locale locale = LocaleContextHolder.getLocale();
        List<String> parts = new ArrayList<>();

        if (hasText(a.getCountry())) parts.add(a.getCountry());
        if (hasText(a.getState())) parts.add(a.getState());
        if (hasText(a.getCity())) parts.add(a.getCity());
        if (hasText(a.getVillage())) parts.add(a.getVillage());
        if (hasText(a.getRegion()))
            parts.add(label("address.region", locale) + " " + a.getRegion());
        if (hasText(a.getStreet())) parts.add(a.getStreet());

        if (hasText(a.getPlatesNumber()))
            parts.add(label("address.plate", locale) + " " + a.getPlatesNumber());

        if (hasText(a.getFloor()))
            parts.add(label("address.floor", locale) + " " + a.getFloor());

        if (hasText(a.getUnit()))
            parts.add(label("address.unit", locale) + " " + a.getUnit());

        if (hasText(a.getPostalCode()))
            parts.add(label("address.postalCode", locale) + " " + a.getPostalCode());

        return String.join("، ", parts);
    }

    private String label(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
