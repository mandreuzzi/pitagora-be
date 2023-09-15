package it.apeiron.pitagora.core.controller;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.ALARMS;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_SET;

import it.apeiron.pitagora.core.dto.AlarmDTO;
import it.apeiron.pitagora.core.dto.QueryEventsDTO;
import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.service.ServiceProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping("alarm")
public class AlarmController {

    private final ServiceProvider sp;

    @GetMapping("/{datasetId}")
    public ResponseEntity<ResponseDTO> getByDatasetId(@PathVariable("datasetId") ObjectId datasetId) {
        return ResponseDTO.ok(sp.alarmService.getAlarmsByDatasetId(datasetId));
    }

    @PostMapping("/{datasetId}")
    public ResponseEntity<ResponseDTO> setAlarmsOnDataset(@PathVariable("datasetId") ObjectId datasetId, @RequestBody List<AlarmDTO> dtos) {
        sp.alarmService.setAlarmsOnDataset(datasetId, dtos);
        return ResponseDTO.created(t(ALARMS, SUCCESSFULLY_SET));
    }

    @GetMapping()
    public ResponseEntity<ResponseDTO> getAllDatasetAlarmsDetail() {
        return ResponseDTO.ok(sp.alarmService.getAllDatasetAlarmsDetail());
    }

    @PutMapping("/set/{datasetId}")
    public ResponseEntity<ResponseDTO> changeAllAlarmsAction(@PathVariable("datasetId") ObjectId datasetId) {
        return ResponseDTO.ok(null, sp.alarmService.changeAlarmsStatus(datasetId));
    }

    @PostMapping("/event")
    public ResponseEntity<ResponseDTO> getPagedEventByQuery(@RequestBody QueryEventsDTO query) {
        return ResponseDTO.ok(sp.alarmEventService.getPagedEventsByQuery(query));
    }

    @PostMapping("/event/alarm/{alarmId}")
    public ResponseEntity<ResponseDTO> checkAlarmOnDataset(@PathVariable("alarmId") ObjectId alarmId) {
        return ResponseDTO.ok(sp.alarmService.checkAlarmOnDataset(alarmId));
    }

    @PostMapping("/event/{datasetId}")
    public ResponseEntity<ResponseDTO> checkAllAlarmOnDataset(@PathVariable("datasetId") ObjectId datasetId) {
        return ResponseDTO.ok(sp.alarmService.checkAllAlarmsOnDataset(datasetId));
    }

    @GetMapping("/severity")
    public ResponseEntity<ResponseDTO> getAlarmsSeverity() {
        return ResponseDTO.ok(sp.alarmEventService.getAlarmsSeverity());
    }

}
