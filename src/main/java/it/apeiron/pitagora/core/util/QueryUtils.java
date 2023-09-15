package it.apeiron.pitagora.core.util;

import static it.apeiron.pitagora.core.entity.enums.FieldType.STRING;
import static it.apeiron.pitagora.core.entity.enums.FieldType.TIMESTAMP;

import it.apeiron.pitagora.core.entity.Filter;
import it.apeiron.pitagora.core.entity.enums.Operation;
import it.apeiron.pitagora.core.exception.PitagoraException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.springframework.data.mongodb.core.query.Criteria;

public class QueryUtils {

    public static final String ISO_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final String REGEX_PLACEHOLDER = "#####";
    private static final String EQUALS_REGEX = "(?i)^" + REGEX_PLACEHOLDER + "$";
    private static final String CONTAINS_REGEX = "(?i).*" + REGEX_PLACEHOLDER + ".*";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(ISO_DATE_PATTERN);

    public static Criteria buildQueryCriteria(Filter filter, String where) {
        Criteria cr = Criteria.where(where);

        if (TIMESTAMP.equals(filter.getFieldType()) && filter.getValue() != null) {
            try {
                filter.setValue(Long.toString(sdf.parse(filter.getValue()).toInstant().toEpochMilli()));
            } catch (ParseException e) {
                throw PitagoraException.internalServerError();
            }
        }

        if (Operation.EQUALS.equals(filter.getOperation())) {
            if (STRING.equals(filter.getFieldType())) {
                return cr.regex(EQUALS_REGEX.replace(REGEX_PLACEHOLDER, filter.getValue()));
            }
            return cr.is(_getNumericFilterValue(filter));
        } else if (Operation.NOT_EQUALS.equals(filter.getOperation())) {
            if (STRING.equals(filter.getFieldType())) {
                return cr.not().regex(EQUALS_REGEX.replace(REGEX_PLACEHOLDER, filter.getValue()));
            }
            return cr.ne(_getNumericFilterValue(filter));
        } else if (Operation.CONTAINS.equals(filter.getOperation())) {
            return cr.regex(CONTAINS_REGEX.replace(REGEX_PLACEHOLDER, filter.getValue()));
        } else if (Operation.NOT_CONTAINS.equals(filter.getOperation())) {
            return cr.not().regex(CONTAINS_REGEX.replace(REGEX_PLACEHOLDER, filter.getValue()));
        } else if (Operation.IS_NULL.equals(filter.getOperation())) {
            return cr.isNull();
        } else if (Operation.IS_TRUE.equals(filter.getOperation())) {
            return cr.is(true);
        } else if (Operation.IS_FALSE.equals(filter.getOperation())) {
            return cr.is(false);
        } else if (Operation.NOT_IS_NULL.equals(filter.getOperation())) {
            return cr.ne(null);
        } else if (Operation.IS_BLANK.equals(filter.getOperation())) {
            return cr.regex(EQUALS_REGEX.replace(REGEX_PLACEHOLDER, ""));
        } else if (Operation.NOT_IS_BLANK.equals(filter.getOperation())) {
            return cr.not().regex(EQUALS_REGEX.replace(REGEX_PLACEHOLDER, ""));
        } else {
            switch (filter.getOperation()) {
                case GT:
                    return cr.gt(_getNumericFilterValue(filter));
                case GTE:
                    return cr.gte(_getNumericFilterValue(filter));
                case LT:
                    return cr.lt(_getNumericFilterValue(filter));
                case LTE:
                    return cr.lte(_getNumericFilterValue(filter));
            }
        }
        throw PitagoraException.internalServerError();
    }

    private static Object _getNumericFilterValue(Filter filter) {
        return TIMESTAMP.equals(filter.getFieldType()) ?
                Long.parseLong(filter.getValue()) : Double.parseDouble(filter.getValue());
    }
}
