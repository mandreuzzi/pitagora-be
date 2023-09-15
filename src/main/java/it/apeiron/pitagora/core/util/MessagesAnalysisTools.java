package it.apeiron.pitagora.core.util;

import java.util.Map;
import lombok.AllArgsConstructor;

import static it.apeiron.pitagora.core.util.Language.*;

@AllArgsConstructor
public enum MessagesAnalysisTools {

    NOT_NULL_RECORD(Map.of(IT, "Numero di Record non NULL", EN, "Number of not NULL records", PT, "Número do registro não NULL")),
    NULL_RECORD(Map.of(IT, "Numero di Record NULL", EN, "Number of NULL records", PT, "Número do registro NULL")),
    MIN(Map.of(IT, "Minimo", EN, "Min", PT, "Mínimo")),
    MAX(Map.of(IT, "Massimo", EN, "Max", PT, "Máximo")),
    SUM(Map.of(IT, "Somma", EN, "Sum", PT, "Soma")),
    MEAN(Map.of(IT, "Media", EN, "Mean", PT, "Média")),
    GEOM_MEAN(Map.of(IT, "Media geometrica", EN, "Geometric mean", PT, "Média geométrica")),
    SUM_OF_SQUARES(Map.of(IT, "Somma dei quadrati", EN, "Sum of squares", PT, "Soma de quadrados")),
    STD_DEV(Map.of(IT, "Deviazione Standard", EN, "Standard deviation", PT, "Desvio padrão")),
    VARIANCE(Map.of(IT, "Varianza", EN, "Variance", PT, "Variação")),
    POP_VARIANCE(Map.of(IT, "Varianza della popolazione", EN, "Population variance", PT, "Variação da população")),
    SKEW(Map.of(IT, "Asimmetria", EN, "Skewness", PT, "Assimetria")),
    KURT(Map.of(IT, "Curtosi", EN, "Kurtosis", PT, "Curtose")),
    NULL_BLANK_RECORDS(Map.of(IT, "Numero di record NULL/BLANK", EN, "Number of NULL/BLANK records", PT , "Número de registros NULL/BLANK"));

    private final Map<Language, String> translations;

    public String translateInto(Language language) {
        return translations.get(language);
    }

}
