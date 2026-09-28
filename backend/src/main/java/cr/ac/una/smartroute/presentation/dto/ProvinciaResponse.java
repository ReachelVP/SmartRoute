package cr.ac.una.smartroute.presentation.dto;

import cr.ac.una.smartroute.data.entity.Provincia;

public record ProvinciaResponse(Long id, String nombre) {

    public static ProvinciaResponse from(Provincia provincia) {
        return new ProvinciaResponse(provincia.getId(), provincia.getNombre());
    }
}
