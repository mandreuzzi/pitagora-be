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
public class ReactiveAccountingModel extends PitagoraModel {

    public static ReactiveAccountingModel create() {
        return new ReactiveAccountingModel();
    }

    private ReactiveAccountingModel() {
        locked = true;
        this.name = ThmDireEntity.REACTIVE_ACCOUNTING.pitagoraName();
        this.scope.add(Theorem.ANALYSIS_TOOLS);
        this.scope.add(Theorem.DIRE);
        structure = new LinkedHashMap<>();

        Arrays.asList(
                ModelField.builder().name("sourceName").description("Nome risorsa").type(FieldType.STRING).build(),
                ModelField.builder().name("reviewStatus").description("Stato di revisione").type(FieldType.STRING).build(),
                ModelField.builder().name("reviewedAt").description("Revisionato il").type(FieldType.TIMESTAMP).build(),
                ModelField.builder().name("id").description("Id").type(FieldType.STRING).build(),
                ModelField.builder().name("ticketId").description("Ticket Id").type(FieldType.STRING).build(),
                ModelField.builder().name("sede").description("Sede").type(FieldType.STRING).build(),
                ModelField.builder().name("piano").description("Piano").type(FieldType.STRING).build(),
                ModelField.builder().name("zona").description("Zona").type(FieldType.STRING).build(),
                ModelField.builder().name("categoria").description("Categoria").type(FieldType.STRING).build(),
                ModelField.builder().name("componente").description("Componente").type(FieldType.STRING).build(),
                ModelField.builder().name("descrizioneProblema").description("Descrizione problema").type(FieldType.STRING).build(),
                ModelField.builder().name("costi").description("Costi").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("materiali").description("Materiali").type(FieldType.STRING).build(),
                ModelField.builder().name("note").description("Note").type(FieldType.STRING).build(),
                ModelField.builder().name("commentoBuildingManager").description("Commento Building Mgr").type(FieldType.STRING).build(),
                ModelField.builder().name("xac").description("Xac").type(FieldType.STRING).build()
        )
                .forEach(field -> structure.put(field.getName(), field));
    }

}
