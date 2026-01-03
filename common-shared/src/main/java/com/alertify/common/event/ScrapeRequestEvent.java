package com.alertify.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScrapeRequestEvent implements Serializable {
    private UUID productId;
    private String url;
}
