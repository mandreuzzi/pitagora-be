package it.apeiron.pitagora.core.theorems.vedo;

import it.apeiron.pitagora.core.dto.HttpResponseDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Data
public class DahuaFinder {

    private final String CREATE_URL;
    private final String FIND_BASE_URL;
    private final String NEXT_URL;
    private final String CLOSE_URL;
    private final String DESTROY_URL;
    private final int NEXT_COUNT = 100; // MAX 100

    private String finderIdParam;
    private final RestTemplate rt;

    public static List<List<String>> find(RestTemplate restTemplate, String dahuaHost) {
        DahuaFinder df = new DahuaFinder(restTemplate, dahuaHost);
        df._getFinder();
        return df._find();
    }

    private DahuaFinder(RestTemplate restTemplate, String dahuaHost) {
        rt = restTemplate;
        String baseUrl = "http://" + dahuaHost + "/cgi-bin/mediaFileFind.cgi?action=";
        CREATE_URL = baseUrl + "factory.create";
        FIND_BASE_URL = baseUrl + "findFile";
        NEXT_URL = baseUrl + "findNextFile";
        CLOSE_URL = baseUrl + "close";
        DESTROY_URL = baseUrl + "destroy";
    }

    private void _getFinder() {
        ResponseEntity<String> res = rt.getForEntity(CREATE_URL, String.class);
        String finderId = res.getBody().replace("result=", "").replace("\r", "").replace("\n", "");
        finderIdParam = "&object=" + finderId;
    }

    private List<List<String>> _find() {
        String FIND_URL = FIND_BASE_URL + finderIdParam
                +  "&condition.Channel=1"
                + "&condition.StartTime=2022-11-24 10:00:00"
                + "&condition.EndTime=2022-11-25 18:00:00"
                + "&condition.Flags[0]=Event"
                + "&condition.Events[0]=FaceDetection"
                + "&condition.DB.FaceDetectionRecordFilter.Beard=0";
        rt.getForEntity(FIND_URL, String.class);

        List<List<String>> data = _getAll();

        _destroyFinder();

        return data;
    }

    private List<List<String>> _getAll() {
        List<List<String>> allDataAsList = new ArrayList<>();

        while (true){
            ResponseEntity<String> dataResponse = rt.getForEntity(NEXT_URL + finderIdParam + "&count=" + NEXT_COUNT, String.class);
            String dataAsString = dataResponse.getBody();
            if (!dataAsString.contains("item")) {
                return allDataAsList;
            }

            List<String> currentDataAsList = Arrays.asList(dataAsString.split("\r\n"));
            currentDataAsList = currentDataAsList.subList(1, currentDataAsList.size());

            List<String> currentItemList = new ArrayList<>();
            int count = 0;
            for (String d : currentDataAsList) {
                String currField = d.substring(d.indexOf("."));
                if (d.startsWith("items[" + count + "]")) {
                    currentItemList.add(currField);
                } else {
                    allDataAsList.add(currentItemList);
                    currentItemList = new ArrayList<>();
                    currentItemList.add(currField);
                    count++;
                }
            }
            allDataAsList.add(currentItemList);

        }

    }

    private void _destroyFinder() {
        rt.getForEntity(CLOSE_URL + finderIdParam, String.class);
        rt.getForEntity(DESTROY_URL + finderIdParam, String.class);
    }


    public static HttpResponseDTO test(RestTemplate restTemplate, String dahuaHost) {
        DahuaFinder df = new DahuaFinder(restTemplate, dahuaHost);

        try {
            df._getFinder();
        } catch (HttpClientErrorException e) {
            return HttpResponseDTO.builder()
                    .statusCode(e.getRawStatusCode())
                    .responseBody(e.getMessage()).build();
        } catch (Exception e) {
            return HttpResponseDTO.builder()
                    .responseBody(e.getMessage()).build();
        }

        return df._test();
    }

    private HttpResponseDTO _test() {
        String FIND_URL = FIND_BASE_URL + finderIdParam
                +  "&condition.Channel=1"
                + "&condition.StartTime=2022-08-23 14:00:00"
                + "&condition.EndTime=2022-08-24 14:00:30"
                + "&condition.Flags[0]=Event"
                + "&condition.Events[0]=FaceDetection"
                + "&condition.DB.FaceDetectionRecordFilter.Beard=0";
        rt.getForEntity(FIND_URL, String.class);

        HttpResponseDTO res;
        try {
            ResponseEntity<String> dataResponse = rt.getForEntity(NEXT_URL + finderIdParam + "&count=" + NEXT_COUNT, String.class);
            res = HttpResponseDTO.builder()
                    .statusCode(dataResponse.getStatusCodeValue())
                    .responseBody(dataResponse.getBody()).build();
        } catch (HttpClientErrorException e) {
            res = HttpResponseDTO.builder()
                    .statusCode(e.getRawStatusCode())
                    .responseBody(e.getMessage()).build();
        } catch (Exception e) {
            res = HttpResponseDTO.builder()
                    .responseBody(e.getMessage()).build();
        }

        _destroyFinder();

        return res;
    }
}
