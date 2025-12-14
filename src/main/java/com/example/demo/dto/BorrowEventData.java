package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BorrowEventData {

    @JsonProperty("notification_data")
    private BorrowNotificationDataEvent notificationData;

    @JsonProperty("inventory_data")
    private InventoryDataEvent inventoryData;
}