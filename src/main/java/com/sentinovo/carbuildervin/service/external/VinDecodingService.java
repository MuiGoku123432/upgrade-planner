package com.sentinovo.carbuildervin.service.external;

import com.sentinovo.carbuildervin.exception.ExternalServiceException;
import com.sentinovo.carbuildervin.exception.ValidationException;
import com.sentinovo.carbuildervin.validation.VinValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VinDecodingService {

    @Qualifier("vinDecoderWebClient")
    private final WebClient vinDecoderWebClient;

    @Value("${app.marketcheck.api.key:}")
    private String marketCheckApiKey;

    @Value("${app.marketcheck.api.base-url:https://api.marketcheck.com}")
    private String marketCheckBaseUrl;

    @Value("${app.vin.validation.enabled:true}")
    private boolean vinValidationEnabled;

    private static final String VIN_DECODE_ENDPOINT = "/v2/decode/car/{vin}/specs";

    public VinDecodingResponse decodeVin(String vin) {
        log.info("Decoding VIN: {}", vin);
        
        validateVin(vin);
        
        try {
            // MarketCheck API uses api_key query parameter for authentication
            String url = marketCheckBaseUrl + VIN_DECODE_ENDPOINT.replace("{vin}", vin) + "?api_key=" + marketCheckApiKey;

            @SuppressWarnings("unchecked")
            Mono<Map> responseMono = vinDecoderWebClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class);

            Map<String, Object> responseBody = responseMono.block();

            if (responseBody == null) {
                throw new ExternalServiceException("MarketCheck", 200, "Empty response received");
            }

            VinDecodingResponse result = parseResponse(responseBody, vin);
            log.info("Successfully decoded VIN: {} - {} {} {}", vin, result.getMake(), result.getModel(), result.getYear());
            return result;

        } catch (WebClientResponseException e) {
            log.error("HTTP error while decoding VIN {}: {} - {}", vin, e.getStatusCode(), e.getMessage());
            
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ValidationException("Invalid or unrecognized VIN: " + vin);
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new ExternalServiceException("MarketCheck", e.getStatusCode().value(), "Authentication failed");
            } else {
                throw new ExternalServiceException("MarketCheck", e.getStatusCode().value(), "HTTP error: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("Unexpected error while decoding VIN {}: {}", vin, e.getMessage());
            throw new ExternalServiceException("MarketCheck", "Unexpected error: " + e.getMessage(), e);
        }
    }

    public boolean isValidVin(String vin) {
        if (!vinValidationEnabled) {
            return true;
        }
        
        return VinValidator.isValidVin(vin);
    }

    public String normalizeVin(String vin) {
        return VinValidator.normalizeVin(vin);
    }

    public boolean isServiceAvailable() {
        if (marketCheckApiKey == null || marketCheckApiKey.trim().isEmpty()) {
            log.warn("MarketCheck API key not configured - VIN decoding service unavailable");
            return false;
        }
        
        try {
            // Test with a known valid VIN
            String testVin = "1HGBH41JXMN109186";
            decodeVin(testVin);
            return true;
        } catch (Exception e) {
            log.warn("MarketCheck service availability check failed: {}", e.getMessage());
            return false;
        }
    }

    private void validateVin(String vin) {
        if (vin == null || vin.trim().isEmpty()) {
            throw new ValidationException("VIN cannot be empty");
        }
        
        String normalizedVin = normalizeVin(vin);
        
        if (!isValidVin(normalizedVin)) {
            throw new ValidationException("Invalid VIN format: " + vin);
        }
        
        if (marketCheckApiKey == null || marketCheckApiKey.trim().isEmpty()) {
            throw new ExternalServiceException("MarketCheck", "API key not configured");
        }
    }


    private VinDecodingResponse parseResponse(Map<String, Object> responseBody, String vin) {
        try {
            // MarketCheck Basic VIN Decoder returns data at root level
            String make = extractStringValue(responseBody, "make");
            String model = extractStringValue(responseBody, "model");
            Integer year = extractIntegerValue(responseBody, "year");
            String trim = extractStringValue(responseBody, "trim");
            String bodyType = extractStringValue(responseBody, "body_type");
            String engine = extractStringValue(responseBody, "engine");
            String transmission = extractStringValue(responseBody, "transmission");
            String drivetrain = extractStringValue(responseBody, "drivetrain");
            String fuelType = extractStringValue(responseBody, "fuel_type");

            return VinDecodingResponse.builder()
                    .vin(vin)
                    .make(make)
                    .model(model)
                    .year(year)
                    .trim(trim)
                    .bodyType(bodyType)
                    .engine(engine)
                    .transmission(transmission)
                    .drivetrain(drivetrain)
                    .fuelType(fuelType)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing VIN decode response for {}: {}", vin, e.getMessage());
            throw new ExternalServiceException("MarketCheck", 200, "Response parsing error: " + e.getMessage());
        }
    }

    private String extractStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }

    private Integer extractIntegerValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        
        if (value instanceof Integer) {
            return (Integer) value;
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Could not parse integer value for key {}: {}", key, value);
            return null;
        }
    }


    public static class VinDecodingResponse {
        private String vin;
        private String make;
        private String model;
        private Integer year;
        private String trim;
        private String bodyType;
        private String engine;
        private String transmission;
        private String drivetrain;
        private String fuelType;
        private boolean success;

        public static VinDecodingResponseBuilder builder() {
            return new VinDecodingResponseBuilder();
        }

        public String getVin() { return vin; }
        public String getMake() { return make; }
        public String getModel() { return model; }
        public Integer getYear() { return year; }
        public String getTrim() { return trim; }
        public String getBodyType() { return bodyType; }
        public String getEngine() { return engine; }
        public String getTransmission() { return transmission; }
        public String getDrivetrain() { return drivetrain; }
        public String getFuelType() { return fuelType; }
        public boolean isSuccess() { return success; }

        public void setVin(String vin) { this.vin = vin; }
        public void setMake(String make) { this.make = make; }
        public void setModel(String model) { this.model = model; }
        public void setYear(Integer year) { this.year = year; }
        public void setTrim(String trim) { this.trim = trim; }
        public void setBodyType(String bodyType) { this.bodyType = bodyType; }
        public void setEngine(String engine) { this.engine = engine; }
        public void setTransmission(String transmission) { this.transmission = transmission; }
        public void setDrivetrain(String drivetrain) { this.drivetrain = drivetrain; }
        public void setFuelType(String fuelType) { this.fuelType = fuelType; }
        public void setSuccess(boolean success) { this.success = success; }

        public static class VinDecodingResponseBuilder {
            private VinDecodingResponse response = new VinDecodingResponse();

            public VinDecodingResponseBuilder vin(String vin) { response.vin = vin; return this; }
            public VinDecodingResponseBuilder make(String make) { response.make = make; return this; }
            public VinDecodingResponseBuilder model(String model) { response.model = model; return this; }
            public VinDecodingResponseBuilder year(Integer year) { response.year = year; return this; }
            public VinDecodingResponseBuilder trim(String trim) { response.trim = trim; return this; }
            public VinDecodingResponseBuilder bodyType(String bodyType) { response.bodyType = bodyType; return this; }
            public VinDecodingResponseBuilder engine(String engine) { response.engine = engine; return this; }
            public VinDecodingResponseBuilder transmission(String transmission) { response.transmission = transmission; return this; }
            public VinDecodingResponseBuilder drivetrain(String drivetrain) { response.drivetrain = drivetrain; return this; }
            public VinDecodingResponseBuilder fuelType(String fuelType) { response.fuelType = fuelType; return this; }
            public VinDecodingResponseBuilder success(boolean success) { response.success = success; return this; }

            public VinDecodingResponse build() { return response; }
        }
    }
}