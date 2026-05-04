package sisosolsol.greenfire.auth.dto;

import sisosolsol.greenfire.spark.model.dto.SparkInfo;

import java.util.UUID;

public record MeResponse(
        boolean ok,
        UUID userId,
        String email,
        String role,
        SparkInfo spark
) {}
