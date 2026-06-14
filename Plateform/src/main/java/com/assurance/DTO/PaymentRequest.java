package com.assurance.DTO;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long contratId;
    private Long montant; // En centimes (Stripe veut des centimes)
}
