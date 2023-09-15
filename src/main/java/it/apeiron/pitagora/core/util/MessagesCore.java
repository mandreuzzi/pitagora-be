package it.apeiron.pitagora.core.util;

import static it.apeiron.pitagora.core.util.Language.EN;
import static it.apeiron.pitagora.core.util.Language.IT;
import static it.apeiron.pitagora.core.util.Language.PT;

import java.util.Map;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum MessagesCore {

    USER(Map.of(IT, "Utente", EN, "User", PT, "Usuário")),
    SOURCE(Map.of(IT, "Fonte Dati", EN, "Data Source", PT, "Fonte de dados")),
    THE_HTTP_DATA_SOURCE(Map.of(IT, "la Fonte dati Http", EN, "the Http Data Source", PT, "Fonte de dados HTTP")),
    THE_FILE(Map.of(IT, "il File", EN, "the File", PT, "O arquivo")),
    THE_EXPOSED_API_DATA_SOURCE(Map.of(IT, "la Fote Dati Pitagora API", EN, "the Pitagora API Data Source", PT, "a fonte de dados da API Pitagora")),
    MODEL(Map.of(IT, "Modello", EN, "Model", PT, "Modelo")),
    THE_MODEL(Map.of(IT, "il Modello", EN, "the Model", PT, "O modelo")),
    MAPPER(Map.of(IT, "Mapper", EN, "Mapper", PT, "Mapeador")),
    THE_MAPPER(Map.of(IT, "il Mapper", EN, "the Mapper", PT, "o Mapeador")),
    DATASET(Map.of(IT, "Dataset", EN, "Dataset", PT, "Dataset")),
    THE_DATASET(Map.of(IT, "il Dataset", EN, "the Dataset", PT, "O conjunto de dados")),
    ALARMS(Map.of(IT, "Allarmi", EN, "Alarms", PT, "Alarmes")),
    ALARM_EVENTS(Map.of(IT, "Eventi d'allarme", EN, "Alarm's events", PT, "eventos de alarme")),
    SUCCESSFULLY_CREATED(Map.of(IT, "creato con successo", EN, "successfully created", PT, "criado com sucesso")),
    SUCCESSFULLY_CREATED_F(Map.of(IT, "creata con successo", EN, "successfully created", PT, "criado com sucesso")),
    SUCCESSFULLY_UPDATED(Map.of(IT, "aggiornato con successo", EN, "successfully updated", PT, "atualizado com sucesso")),
    SUCCESSFULLY_UPDATED_F(Map.of(IT, "aggiornata con successo", EN, "successfully updated", PT, "atualizado com sucesso")),
    SUCCESSFULLY_DELETED(Map.of(IT, "eliminato con successo", EN, "successfully deleted", PT, "excluído com sucesso")),
    SUCCESSFULLY_DELETED_F(Map.of(IT, "eliminata con successo", EN, "successfully deleted", PT, "excluído com sucesso")),
    SUCCESSFULLY_SET(Map.of(IT, "settati con successo", EN, "successfully set", PT, "configurado com sucesso")),
    SUCCESSFULLY_CHANGED_F(Map.of(IT, "modificata con successo", EN, "successfully updated", PT, "modificado com sucesso")),
    SUCCESSFULLY_COMPLETED(Map.of(IT, "completato con successo", EN, "successfully completed", PT, "concluído com sucesso")),
    LOCKED_SYSTEM_OBJECT(Map.of(
            IT, "Questo è un oggetto di sistema e non può essere modificato",
            EN, "This is a system resource and can not be edited",
            PT, "Este é um objeto do sistema e não pode ser modificado")),

    NAME_CAN_NOT_START_WITH_PITAGORA(Map.of(
            IT, "Il nome non può iniziare con la parola 'pitagora'",
            EN, "The name can not start with the word 'pitagora'",
            PT, "O nome não pode começar com a palavra 'pitagora'")),
    SYSTEM_RESOURCE(Map.of(
            IT, " (risorsa di sistema)",
            EN, " (system resource)",
            PT, " (recurso do sistema)")),
    FILE_NOT_VALID_MISSING_COLUMN(Map.of(
            IT, "Il file non è conforme: su una o più righe la colonna numero $COLUMN_NUMBER$ è assente",
            EN, "File not valid: on one or more rows the column number $COLUMN_NUMBER$ is missing",
            PT, "O arquivo não é compatível: em uma ou mais linhas o número da coluna $COLUMN_NUMBER$ está ausente")),
    FILE_EXTENSION_NOT_VALID(Map.of(
            IT, "Formato file non valido. I tipi ammessi sono: .xls, .xlsx",
            EN, "File not valid: only .xls and .xlsx are allowed",
            PT, "Formato de arquivo inválido. Os tipos permitidos são: .xls, .xlsx")),
    FILE_WRONG_ROW_COL_NUM(Map.of(
            IT, "Il numero della riga/colonna iniziale deve essere minore o uguale a quella finale",
            EN, "Starting row/column number must be less or equal than the ending one",
            PT, "O número da linha/coluna inicial deve ser menor ou igual ao final")),
    NAME_NOT_AVAILABLE(Map.of(IT, "Questo nome è già occupato", EN, "This name is not available", PT, "Este nome já está ocupado")),
    SOURCE_MUST_BE_HTTP(Map.of(IT, "La Fonte Dati non è Http/Https", EN, "Data Source must be Http/Https", PT, "A fonte de dados não é Http/Https")),
    MODEL_CHANGES_NOT_ALLOWED(Map.of(
            IT, "Le modifiche apportate non sono ammesse poichè questo modello è collegato ad altri Mapper e/o Dataset",
            EN, "These changes are not allowed because the Model is linked to others Mapper and/or Dataset",
            PT, "As alterações feitas não são permitidas pois este modelo está conectado a outros Mappers e/ou Datasets"
    )),
    JSON_ROOT_TYPE_DETECTION_FAILED(Map.of(
            IT, "Impossibile elaborare il json (object/array detection failed)",
            EN, "Json root type (object/array) detection failed",
            PT, "Não foi possível processar o json (falha na detecção de objeto/array)")),
    RULE_ON_MISSING_MODEL_FIELD(Map.of(
            IT, "Una o più regole riferiscono campi del Modello inesistenti",
            EN, "One or more Rules relate to fields not present in the Model",
            PT, "Uma ou mais regras referem-se a campos do Modelo que não existem")),
    CHANNEL_NOT_NEED_MAPPER(Map.of(
            IT, "Questo canale non richiede qualifica dei dati",
            EN, "This type of Data Source does not need a Mapper",
            PT, "Este canal não requer qualificação de dados")),
    DATASET_NAME_NOT_VALID(Map.of(
            IT, "Il nome del Dataset non può iniziare con 'system.' e non può contenere il carattere dollaro '$'",
            EN, "Dataset name can not start with 'system.' and can not contain '$'",
            PT, "O nome do conjunto de dados não pode começar com 'system.' e não pode conter o caractere de dólar '$'")),
    MAPPER_AND_SOURCE_UNRELATED(Map.of(
            IT, "Il Mapper e la Fonte Dati selezionati non sono collegati",
            EN, "Mapper and Data Source are unrelated",
            PT, "Mapeador e fonte de dados selecionados não estão vinculados")),
    MAPPER_AND_DATASET_MODEL_MISMATCH(Map.of(
            IT, "Il Mapper selezionato e il Modello collegato al Dataset non corrispondono",
            EN, "Mapper and Dataset's Model mismatch",
            PT, "O mapeador selecionado e o modelo conectado ao conjunto de dados não correspondem")),
    MAPPER_NOT_HTTP(Map.of(
            IT, "Questo Mapper non è per una Fonte Dati Http",
            EN, "This Mapper is not for Http Source",
            PT, "Este mapeador não é para uma fonte de dados HTTP")),
    ERROR_HTTPSOURCE_SCHEDULING(Map.of(
            IT, "Si sono verificati errori nella programmazione delle chiamate API",
            EN, "Errors on scheduling the API calls",
            PT, "Ocorreram erros na programação das chamadas da API")),
    EXPORTED_ON(Map.of(IT, "Export del: ", EN, "Exported on: ", PT, "Exportação de: ")),
    ALARMS_HAVE_BEEN(Map.of(IT, "Tutti gli allarmi sono stati ", EN, "All alarms have been ", PT, "Todos os alarmes foram ")),
    ENABLED(Map.of(IT, "attivati", EN, "enabled", PT, "ativado")),
    DISABLED(Map.of(IT, "disattivati", EN, "disabled", PT, "desativado")),
    ALARMS_MAIL_SUBJECT(Map.of(IT, "Allarmi sul Dataset ", EN, "Alarms on Dataset ", PT, "Alarmes no conjunto de dados ")),
    ALARMS_MAIL_TITLE(Map.of(
            IT, "Attenzione! <br>Sono stati rilevati <strong>NUM_OF_EVENTS</strong> eventi d'allarme sul Dataset <strong>DATASET_NAME</strong>:<br><br>",
            EN, "Warning! <br><strong>NUM_OF_EVENTS</strong> alarm events have been detected on Dataset <strong>DATASET_NAME</strong>:<br><br>",
            PT, "Atenção! <br><strong>NUM_OF_EVENTS</strong> eventos de alarme foram detectados no conjunto de dados <strong>DATASET_NAME</strong>:<br><br>")),
    ALARMS_MAIL_ENTRIES(Map.of(
            IT, " eventi per l'allarme ",
            EN, " events on alarm ",
            PT, " eventos de alarme ")),
    NUMBER_FORMAT_EX_TITLE(Map.of(
            IT, "Non è possibile creare il Mapper: \n"
                    + "il campo del modello di riferimento 'FIELD_DESCRIPTION' (dichiarato di tipo FIELD_TYPE_DESCRIPTION) non trova corrispondenza con l'elemento mappato ",
            EN, "Mapper can not be created: \n" +
                    "Model's field 'FIELD_DESCRIPTION' (defined as FIELD_TYPE_DESCRIPTION) does not match with the element ",
            PT, "O mapeador não pode ser criado: \n"
                    + "o campo do modelo de referência 'FIELD_DESCRIPTION' (declarado do tipo FIELD_TYPE_DESCRIPTION) não corresponde ao elemento mapeado ")),
    EXC_DETAIL_JSON(Map.of(
            IT, "sul Json alla posizione 'DETAIL'",
            EN, "in the Json located at 'DETAIL'",
            PT, "no Json para a posição 'DETAIL'")),
    EXC_DETAIL_EXCEL(Map.of(
            IT, "sull'Excel alla colonna numero 'DETAIL'",
            EN, "in the Excel at the column number 'DETAIL'",
            PT, "no Excel para o número da coluna 'DETAIL'")),
    EXC_DETAIL_CSV(Map.of(
            IT, "sul CSV alla colonna numero 'DETAIL'",
            EN, "in the CSV at the column number 'DETAIL'",
            PT, "no CSV para o número da coluna 'DETAIL'")),
    TIMESTAMP_EX_TITLE(Map.of(
            IT, "Non è possibile creare il Mapper:\nerrore nel parsing della data ",
            EN, "Mapper can not be created:\nerror parsing the date ",
            PT, "O mapeador não pode ser criado:\nerro ao analisar a data")),
    TIMESTAMP_EX_FIELD_DETAIL(Map.of(
            IT, " relativa al campo 'FIELD_DESCRIPTION' del modello di riferimento (FIELD_TYPE_DESCRIPTION). \n",
            EN, " linked to Model's field 'FIELD_DESCRIPTION' (FIELD_TYPE_DESCRIPTION). \n",
            PT, " relativo ao campo 'FIELD_DESCRIPTION' do modelo de referência (FIELD_TYPE_DESCRIPTION). \n"));

    private final Map<Language, String> translations;

    public String translateInto(Language language) {
        return translations.get(language);
    }

}
