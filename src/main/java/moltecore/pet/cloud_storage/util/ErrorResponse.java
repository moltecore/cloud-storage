package moltecore.pet.cloud_storage.util;

public record ErrorResponse(
        String message,
        long timestamp
) {}