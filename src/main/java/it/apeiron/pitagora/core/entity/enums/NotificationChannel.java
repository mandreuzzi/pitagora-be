package it.apeiron.pitagora.core.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum NotificationChannel {

    NULL(""),
    EMAIL("Email");
//    WHATSAPP("WhatsApp"),
//    SMS("SMS");

    private String description;

}
