package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.collection.AbstractPitagoraRecord;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class AbstractRecordDTO {

    protected String id;
    protected String name;
    protected boolean locked;
    protected List<Theorem> scope;

    protected void setSuperProps(AbstractPitagoraRecord m) {
        id = m.getId().toString();
        name = m.getNameForClient();
        locked = m.isLocked();
        scope = m.getScope();
    }

}
