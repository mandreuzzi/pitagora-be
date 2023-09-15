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
public class SiteModel extends PitagoraModel {

    public static SiteModel create() {
        return new SiteModel();
    }

    private SiteModel() {
        locked = true;
        this.name = ThmDireEntity.SITE.pitagoraName();
        this.scope.add(Theorem.ANALYSIS_TOOLS);
        this.scope.add(Theorem.DIRE);
        structure = new LinkedHashMap<>();

        Arrays.asList(
                ModelField.builder().name("sede").description("Sede").type(FieldType.STRING).build(),
                ModelField.builder().name("description").description("Descrizione").type(FieldType.STRING).build(),
                ModelField.builder().name("latitude").description("Latitudine").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("longitude").description("Longitudine").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("surface").description("Superficie").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("state").description("Stato").type(FieldType.STRING).build(),
                ModelField.builder().name("region").description("Regione").type(FieldType.STRING).build(),
                ModelField.builder().name("city").description("Città").type(FieldType.STRING).build(),
                ModelField.builder().name("mainAssetClass").description("Classe di attività principale").type(FieldType.STRING).build(),
                ModelField.builder().name("mainAssetClassSurface").description("Superficie della classe di attività principale").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("secondaryAssetClass").description("Classe di attività secondaria").type(FieldType.STRING).build(),
                ModelField.builder().name("secondaryAssetClassSurface").description("Superficie della classe di attività secondaria").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("constructionYear").description("Anno di costruzione").type(FieldType.INTEGER).build(),
                ModelField.builder().name("ownership").description("Proprietà").type(FieldType.INTEGER).build(),
                ModelField.builder().name("acquisitionCost").description("Costo di acquisizione").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("acquisitionYear").description("Anno di acquisizione").type(FieldType.INTEGER).build(),
                ModelField.builder().name("assetValue").description("Valore patrimoniale").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("certification").description("Certificazione").type(FieldType.STRING).build(),
                ModelField.builder().name("level").description("Livello").type(FieldType.STRING).build(),
                ModelField.builder().name("status").description("Status").type(FieldType.STRING).build(),
                ModelField.builder().name("tenantType").description("Tipo di inquilino").type(FieldType.STRING).build(),
                ModelField.builder().name("year").description("Anno").type(FieldType.INTEGER).build(),
                ModelField.builder().name("euroPerMq").description("€/m2").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("commonSpace").description("Spazio comune").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("tenantNumber").description("Numero inquilino").type(FieldType.INTEGER).build(),
                ModelField.builder().name("occupancy").description("Occupazione (mq)").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("tenantOneOccupancy").description("Inquilino 1 Occupazione").type(FieldType.STRING).build(),
                ModelField.builder().name("monthlyRent").description("Affitto mensile (€/mq)").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("unoccupied").description("Disoccupato").type(FieldType.BOOLEAN).build(),
                ModelField.builder().name("vacancyRate").description("Tasso di posto vacante").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("assetClassVacancyRate").description("Tasso di disponibilità della classe di attività").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("gri").description("GRI").type(FieldType.STRING).build()
                )
                .forEach(field -> structure.put(field.getName(), field));
    }

}
