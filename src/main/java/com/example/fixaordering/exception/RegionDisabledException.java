
package com.example.fixaordering.exception;

import org.springframework.http.HttpStatus;

public class RegionDisabledException extends BusinessException {

    public RegionDisabledException(Long regionId) {
        super("REGION_DISABLED",
                "The region of the selected address (region id %d) is disabled.".formatted(regionId),
                HttpStatus.UNPROCESSABLE_ENTITY);
    }
}

