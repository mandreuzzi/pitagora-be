package it.apeiron.pitagora.core.quartz;

import lombok.Data;

@Data
public class JobDTO {

    private String data;
    private String cronString;

}
