package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnEventData {

    @JsonProperty("notification_data")
    private ReturnNotificationDataEvent returnNotificationDataEvent;

    @JsonProperty("inventory_data")
    private InventoryDataEvent inventoryData;
}