package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.util.MessagesCore.ALARMS_MAIL_ENTRIES;
import static it.apeiron.pitagora.core.util.MessagesCore.ALARMS_MAIL_SUBJECT;
import static it.apeiron.pitagora.core.util.MessagesCore.ALARMS_MAIL_TITLE;

import it.apeiron.pitagora.core.entity.collection.PitagoraAlarm;
import it.apeiron.pitagora.core.entity.collection.PitagoraUser;
import it.apeiron.pitagora.core.entity.enums.NotificationChannel;
import it.apeiron.pitagora.core.util.Language;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.bson.types.ObjectId;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@CommonsLog
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender emailSender;
    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    public void notifyAlarms(Map<ObjectId, Integer> eventCountPerAlarms, List<PitagoraAlarm> allAlarms, String datasetName) {
        Arrays.asList(NotificationChannel.values()).forEach(notificationChannel -> {
            Map<String, Integer> thisChannelEventCount = new HashMap<>();
            eventCountPerAlarms.keySet().forEach(alarmId -> {
                PitagoraAlarm currentAlarm = allAlarms.stream().filter(alarm -> alarm.getId().equals(alarmId)).findFirst().get();
                if (notificationChannel.equals(currentAlarm.getNotification())) {
                    thisChannelEventCount.put(currentAlarm.getName(), eventCountPerAlarms.get(currentAlarm.getId()));
                }
            });

            if (!thisChannelEventCount.isEmpty()) {
                PitagoraUser receiver = this.sp.userService.findReceiverUser();
                if (receiver.getLanguage() == null) {
                    receiver.setLanguage(Language.EN);
                }
                _notifyAlarms(datasetName, thisChannelEventCount, notificationChannel, receiver);
            }
        });
    }

    private void _notifyAlarms(String datasetName, Map<String, Integer> emailAlarms, NotificationChannel channel, PitagoraUser receiver) {
        int allEvents = emailAlarms.values().stream().reduce(0, Integer::sum);

        String subject = "Pitagora - " + receiver.getLanguage()._t(ALARMS_MAIL_SUBJECT) + datasetName;
        StringBuilder text = new StringBuilder(receiver.getLanguage()._t(ALARMS_MAIL_TITLE).replace("NUM_OF_EVENTS", Integer.toString(allEvents)).replace("DATASET_NAME", datasetName));
        for (String alarmName : emailAlarms.keySet()) {
            text.append(String.format("<b>%s</b>", emailAlarms.get(alarmName))).append(receiver.getLanguage()._t(ALARMS_MAIL_ENTRIES)).append(String.format("<b>%s</b>", alarmName)).append("<br>");
        }

        this._sendNotification(receiver.getEmail(), subject, text.toString(), channel);
    }

    private void _sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private void _sendNotification(String to, String subject, String text, NotificationChannel channel) {
        switch (channel) {
            case EMAIL:
                this._sendEmail(to, subject, text);
                break;
            case NULL:
            default:
                break;

        }
    }
}
