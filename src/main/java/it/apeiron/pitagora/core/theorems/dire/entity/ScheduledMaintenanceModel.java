package it.apeiron.pitagora.core.theorems.dire.entity;

import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import java.util.Arrays;
import java.util.LinkedHashMap;
import lombok.Data;

@Data
public class ScheduledMaintenanceModel extends PitagoraModel {

    public static ScheduledMaintenanceModel create() {
        return new ScheduledMaintenanceModel();
    }

    private ScheduledMaintenanceModel() {
        locked = true;
        this.name = ThmDireEntity.SCHEDULED_MAINTENANCE.pitagoraName();
        this.scope.add(Theorem.ANALYSIS_TOOLS);
        this.scope.add(Theorem.DIRE);
        structure = new LinkedHashMap<>();

        Arrays.asList(
                        ModelField.builder().name("sourceName").description("Nome risorsa").type(FieldType.STRING).build(),
                        ModelField.builder().name("reviewStatus").description("Stato di revisione").type(FieldType.STRING).build(),
                        ModelField.builder().name("reviewedAt").description("Revisionato il").type(FieldType.TIMESTAMP).build(),
                        ModelField.builder().name("id").description("Id").type(FieldType.STRING).build(),
                        ModelField.builder().name("tag").description("Tag").type(FieldType.STRING).build(),
                        ModelField.builder().name("componente").description("Componente").type(FieldType.STRING).build(),
                        ModelField.builder().name("item").description("Item").type(FieldType.STRING).build(),
                        ModelField.builder().name("codiceImpianto").description("Codice Impianto").type(FieldType.STRING).build(),
                        ModelField.builder().name("nomeImpianto").description("Nome Impianto").type(FieldType.STRING).build(),
                        ModelField.builder().name("blocchiEdificioOZonaServita").description("Blocchi Edificio O Zona Servita").type(FieldType.STRING).build(),
                        ModelField.builder().name("intervento").description("Intervento").type(FieldType.STRING).build(),
                        ModelField.builder().name("periodicita").description("Periodicità").type(FieldType.STRING).build(),
                        ModelField.builder().name("dataIntervento").description("Data Intervento").type(FieldType.TIMESTAMP).build(),
                        ModelField.builder().name("operatore").description("Operatore").type(FieldType.STRING).build(),
                        ModelField.builder().name("note").description("Note").type(FieldType.STRING).build(),
                        ModelField.builder().name("attachedFiles").description("Attached Files").type(FieldType.STRING).build(),
                        ModelField.builder().name("piano").description("Piano").type(FieldType.STRING).build(),
                        ModelField.builder().name("zona").description("Zona").type(FieldType.STRING).build(),
                        ModelField.builder().name("sede").description("Sede").type(FieldType.STRING).build()
                )
                .forEach(field -> structure.put(field.getName(), field));
    }

}
