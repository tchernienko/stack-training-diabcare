package com.example.diabcare.rdv.domain;

import java.util.UUID;

public record RdvId(UUID value) {

    public static RdvId of(UUID value) {
        return new RdvId(value);
    }

    public static RdvId newOne() {
        return new RdvId(UUID.randomUUID());
    }

}
