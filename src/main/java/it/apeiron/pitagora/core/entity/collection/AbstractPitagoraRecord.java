package it.apeiron.pitagora.core.entity.collection;

import it.apeiron.pitagora.core.dto.AbstractRecordDTO;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.exception.PitagoraException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class AbstractPitagoraRecord {

    public static final String SYSTEM_RESOURCE_NAME_PREFIX = "Pitagora - ";

    @Id
    protected ObjectId id;
    @CreatedDate
    protected LocalDateTime createdAt;
    protected String name;
    protected List<Theorem> scope = new ArrayList<>();

    protected boolean locked = false;

    protected void superUpdate(AbstractRecordDTO d) {
        if (locked) {
            throw PitagoraException.badRequest(t(LOCKED_SYSTEM_OBJECT));
        }
        id = StringUtils.isNotEmpty(d.getId()) ? new ObjectId(d.getId()) : null;
        name = d.getName().trim();
        if (name.toLowerCase().startsWith("pitagora")) {
            throw PitagoraException.notAcceptable(t(NAME_CAN_NOT_START_WITH_PITAGORA));
        }

        if (d.getScope() != null && d.getScope().isEmpty()) {
            scope.add(Theorem.ANALYSIS_TOOLS);
        } else {
            scope = d.getScope();
        }
    }

    public String getNameForClient() {
        return locked ? name.substring(SYSTEM_RESOURCE_NAME_PREFIX.length()) + t(SYSTEM_RESOURCE) : name;
    }
}
