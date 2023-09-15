package it.apeiron.pitagora.core.theorems.dire.dto;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteDTO {

    private String sede;
    private String description;
    private double latitude;
    private double longitude;
    private double mapMarkerRadius;
    private Object metadata;

    public SiteDTO(Map<String, Object> siteRecord) {
        sede = (String) siteRecord.get("sede");
        description = (String) siteRecord.get("description");
        latitude = (double) siteRecord.get("latitude");
        longitude = (double) siteRecord.get("longitude");
    }

    public SiteDTO(String site, String description, double latitude, double longitude, Object metaData){
        this.sede = site;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.metadata = metaData;
    }

    public SiteDTO(String sede) {
        this.sede = sede;
        this.description = this.sede;
    }

    public static List<SiteDTO> calcMarkerRadiusViaMetadata(List<SiteDTO> sites) {
        if(sites.size() == 1){
            sites.get(0).mapMarkerRadius = 1;
            return sites;
        }

        AtomicReference<Double> max = new AtomicReference<>(- Double.MIN_VALUE);
        AtomicReference<Double> min = new AtomicReference<>(Double.MAX_VALUE);
        sites.forEach(site -> {
            double value = (double) site.getMetadata();
            max.set(Math.max(max.get(), value));
            min.set(Math.min(min.get(), value));
        });

        return sites.stream()
                .peek(site ->
                        site.setMapMarkerRadius((((double) site.getMetadata()) - min.get()) / (max.get() - min.get()))
        ).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SiteDTO siteDTO = (SiteDTO) o;
        return sede.equals(siteDTO.sede);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sede);
    }
}
