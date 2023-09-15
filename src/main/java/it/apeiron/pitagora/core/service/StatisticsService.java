package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.entity.enums.FieldType.TIMESTAMP;
import static it.apeiron.pitagora.core.util.Language.tANT;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.GEOM_MEAN;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.KURT;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.MAX;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.MEAN;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.MIN;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.NOT_NULL_RECORD;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.NULL_BLANK_RECORDS;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.NULL_RECORD;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.POP_VARIANCE;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.SKEW;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.STD_DEV;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.SUM;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.SUM_OF_SQUARES;
import static it.apeiron.pitagora.core.util.MessagesAnalysisTools.VARIANCE;
import static java.util.Map.entry;

import it.apeiron.pitagora.core.dto.QueryDatasetDTO;
import it.apeiron.pitagora.core.dto.SimpleRegressionDTO;
import it.apeiron.pitagora.core.dto.StatisticsDTO;
import it.apeiron.pitagora.core.dto.ValueDescriptionDTO;
import it.apeiron.pitagora.core.dto.charts.FieldDataDTO;
import it.apeiron.pitagora.core.dto.charts.HistogramChartDTO;
import it.apeiron.pitagora.core.dto.charts.RequestChartDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StatisticsService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    public StatisticsDTO getStatistics(String columnName, QueryDatasetDTO query) {

        PitagoraModel model = sp.modelService.getDatasetModel(new ObjectId(query.getDatasetId()));
        String columnDescription = model.getStructure().get(columnName).getDescription();
        FieldDataDTO fieldData = sp.dataRecordsService.getDataRecordsAsColumnsByDatasetId(query, columnName, null).get(columnName);

        if (fieldData.isNumerical()) {

            double[] doubles = new double[fieldData.getValues().size()];
            int recNotNull = 0;
            for (int i = 0; i < fieldData.getValues().size(); i++) {
                if (fieldData.getValues().get(i) != null) {
                    doubles[i] = Double.parseDouble(fieldData.getValues().get(i).toString());
                    recNotNull++;
                }
            }

            DescriptiveStatistics ds = new DescriptiveStatistics(doubles);
            StatisticsDTO statistics = new StatisticsDTO(columnDescription);
            statistics.getStats().add(ValueDescriptionDTO.builder().description(tANT(NOT_NULL_RECORD)).value(String.valueOf(recNotNull)).build());
            statistics.getStats().add(ValueDescriptionDTO.builder().description(tANT(NULL_RECORD)).value(String.valueOf(fieldData.getValues().size() - recNotNull)).build());

            if (!TIMESTAMP.equals(fieldData.getField().getType())) {
                Map.ofEntries(
                        entry(MIN, ds.getMin()),
                        entry(MAX, ds.getMax()),
                        entry(MEAN, ds.getMean()),
                        entry(GEOM_MEAN, ds.getGeometricMean()),
                        entry(SUM, ds.getSum()),
                        entry(SUM_OF_SQUARES, ds.getSumsq()),
                        entry(STD_DEV, ds.getStandardDeviation()),
                        entry(VARIANCE, ds.getVariance()),
                        entry(POP_VARIANCE, ds.getPopulationVariance()),
                        entry(SKEW, ds.getSkewness()),
                        entry(KURT, ds.getKurtosis()))
                        .forEach((k,v) -> statistics.getStats().add(new ValueDescriptionDTO(String.valueOf(v), tANT(k))));
            }

            return statistics;

        } else if (fieldData.isCategorical()) {

            StatisticsDTO statistics = new StatisticsDTO(columnDescription);

            RequestChartDTO chart = new RequestChartDTO();
            chart.setQuery(query);
            chart.setColumnNameX(columnName);

            HistogramChartDTO res = sp.chartsService.getHistogramChart(fieldData);

            res.getCategories().forEach(cat -> {
                statistics.getStats().add(ValueDescriptionDTO.builder().description(cat.getCategory())
                        .value(String.valueOf(cat.getCount())).build());
            });
            if (res.getNullCount() > 0) {
                statistics.getStats().add(ValueDescriptionDTO.builder().description(tANT(NULL_BLANK_RECORDS))
                        .value(String.valueOf(res.getNullCount())).build());
            }
            return statistics;
        }
        return null;
    }

    public SimpleRegressionDTO getLinearRegression(RequestChartDTO dto) {
        Map<String, FieldDataDTO> map = sp.dataRecordsService.getDataRecordsAsColumnsByDatasetId(dto.getQuery(), dto.getColumnNameX(), dto.getColumnNameY());

        FieldDataDTO dataX = map.get(dto.getColumnNameX());
        FieldDataDTO dataY = map.get(dto.getColumnNameY());

        double[] doubleX = new double[dataX.getValues().size()];
        double[] doubleY = new double[dataY.getValues().size()];

        for (int i = 0; i < dataX.getValues().size(); i++) {
            if (dataX.getValues().get(i) != null && dataY.getValues().get(i) != null) {
                doubleX[i] = Double.parseDouble(dataX.getValues().get(i).toString());
                doubleY[i] = Double.parseDouble(dataY.getValues().get(i).toString());
            }

        }

        org.apache.commons.math3.stat.regression.SimpleRegression regression = new SimpleRegression();

        double[][] doubles = new double[0][0];

        for (int i = 0; i < doubleX.length; i++) {
            regression.addData(doubleX[i], doubleY[i]);
        }

        regression.addData(doubles);

        double slope = regression.getSlope();
        double intercept = regression.getIntercept();

        return SimpleRegressionDTO.builder().slope(slope).intercept(intercept).build();
    }

}
