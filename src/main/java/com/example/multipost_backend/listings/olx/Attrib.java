package com.example.multipost_backend.listings.olx;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Attrib {
    private String code;
    private String value;
}
