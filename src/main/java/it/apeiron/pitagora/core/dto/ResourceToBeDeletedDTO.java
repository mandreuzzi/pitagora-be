package it.apeiron.pitagora.core.dto;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.ALARMS;
import static it.apeiron.pitagora.core.util.MessagesCore.ALARM_EVENTS;
import static it.apeiron.pitagora.core.util.MessagesCore.THE_DATASET;
import static it.apeiron.pitagora.core.util.MessagesCore.THE_EXPOSED_API_DATA_SOURCE;
import static it.apeiron.pitagora.core.util.MessagesCore.THE_FILE;
import static it.apeiron.pitagora.core.util.MessagesCore.THE_HTTP_DATA_SOURCE;
import static it.apeiron.pitagora.core.util.MessagesCore.THE_MAPPER;
import static it.apeiron.pitagora.core.util.MessagesCore.THE_MODEL;

import it.apeiron.pitagora.core.entity.ExposedApiSource;
import it.apeiron.pitagora.core.entity.FileUploadSource;
import it.apeiron.pitagora.core.entity.HttpSource;
import it.apeiron.pitagora.core.entity.collection.AbstractPitagoraRecord;
import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceToBeDeletedDTO {
    private String type;
    private String name;
    private List<LinkedResource> toBeDeleted;

    public static ResourceToBeDeletedDTO buildForDatasetDeletion(String datasetName, long numOfRecords, int numOfAlarms, int numOfAlarmEvents) {
        return ResourceToBeDeletedDTO.builder()
                .type(t(THE_DATASET))
                .name(datasetName)
                .toBeDeleted(Arrays.asList(
                        LinkedResource.builder()
                                .type(Long.toString(numOfRecords))
                                .names("record")
                                .build(),
                        LinkedResource.builder()
                                .type(Integer.toString(numOfAlarms))
                                .names(t(ALARMS))
                                .build(),
                        LinkedResource.builder()
                                .type(Integer.toString(numOfAlarmEvents))
                                .names(t(ALARM_EVENTS))
                                .build()))
                .build();
    }

    public static ResourceToBeDeletedDTO build(AbstractPitagoraRecord resource,
            List<PitagoraDataset> linkedDataset, boolean deleteDataset, List<PitagoraMapper> linkedMappers) {
        String resourceTypeMsg = "";
        if (resource instanceof PitagoraSource) {
            if (((PitagoraSource) resource).getConfiguration() instanceof HttpSource) {
                resourceTypeMsg += t(THE_HTTP_DATA_SOURCE);
            } else if (((PitagoraSource) resource).getConfiguration() instanceof FileUploadSource) {
                resourceTypeMsg += t(THE_FILE);
            } else if (((PitagoraSource) resource).getConfiguration() instanceof ExposedApiSource) {
                resourceTypeMsg += t(THE_EXPOSED_API_DATA_SOURCE);
            }
        } else if (resource instanceof PitagoraModel) {
            resourceTypeMsg = t(THE_MODEL);
        } else if (resource instanceof PitagoraMapper) {
            resourceTypeMsg = t(THE_MAPPER);
        } else if (resource instanceof PitagoraDataset) {
            resourceTypeMsg = t(THE_DATASET);
        }

        List<LinkedResource> toBeDeleted = new ArrayList<>();
        if (!linkedDataset.isEmpty()) {
            LinkedResource linked = _buildLinked(linkedDataset);
            if (deleteDataset) {
                toBeDeleted.add(linked);
            }
        }
        if (!linkedMappers.isEmpty()) {
            toBeDeleted.add(_buildLinked(linkedMappers));
        }
        return ResourceToBeDeletedDTO.builder()
                .type(resourceTypeMsg)
                .name(resource.getName())
                .toBeDeleted(toBeDeleted)
                .build();
    }

    private static <T> LinkedResource _buildLinked(List<T> linkedResources) {
        String type = "";
        T el = linkedResources.get(0);
        if (el instanceof PitagoraDataset) {
            type = "Dataset";
        } else if (el instanceof PitagoraMapper) {
            type = "Mapper";
        }
        return LinkedResource.builder()
                .type(type)
                .names(linkedResources.stream().map(t -> ((AbstractPitagoraRecord) t).getName()).distinct()
                        .collect(Collectors.joining(", ")))
                .build();
    }

    @Data
    @Builder
    public static class LinkedResource {
        private String type;
        private String names;
    }

}
