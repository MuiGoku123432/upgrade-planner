package com.sentinovo.carbuildervin.validation;

import com.sentinovo.carbuildervin.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]{3,50}$"
    );

    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(String.format("%s cannot be null", fieldName));
        }
    }

    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(String.format("%s cannot be empty", fieldName));
        }
    }

    public static void validateLength(String value, String fieldName, int minLength, int maxLength) {
        if (value != null) {
            int length = value.trim().length();
            if (length < minLength || length > maxLength) {
                throw new ValidationException(
                    String.format("%s must be between %d and %d characters", fieldName, minLength, maxLength)
                );
            }
        }
    }

    public static void validateEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                throw new ValidationException("Invalid email format");
            }
        }
    }

    public static void validateUsername(String username) {
        validateNotEmpty(username, "Username");
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException(
                "Username must be 3-50 characters and contain only letters, numbers, dots, underscores, and hyphens"
            );
        }
    }

    public static void validatePassword(String password) {
        validateNotEmpty(password, "Password");
        
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("Password must contain at least one uppercase letter");
        }
        
        if (!password.matches(".*[a-z].*")) {
            throw new ValidationException("Password must contain at least one lowercase letter");
        }
        
        if (!password.matches(".*[0-9].*")) {
            throw new ValidationException("Password must contain at least one digit");
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new ValidationException("Password must contain at least one special character");
        }
    }

    public static void validateVin(String vin) {
        if (vin != null && !vin.trim().isEmpty()) {
            if (!VinValidator.isValidVin(vin)) {
                throw new ValidationException("Invalid VIN format");
            }
        }
    }

    public static void validatePositiveNumber(Number value, String fieldName) {
        if (value != null) {
            if (value.doubleValue() <= 0) {
                throw new ValidationException(String.format("%s must be positive", fieldName));
            }
        }
    }

    public static void validateNonNegativeNumber(Number value, String fieldName) {
        if (value != null) {
            if (value.doubleValue() < 0) {
                throw new ValidationException(String.format("%s cannot be negative", fieldName));
            }
        }
    }

    public static void validatePriceRange(BigDecimal price, String fieldName) {
        if (price != null) {
            validateNonNegativeNumber(price, fieldName);
            
            // Check for reasonable maximum price (e.g., $1,000,000)
            if (price.compareTo(new BigDecimal("1000000")) > 0) {
                throw new ValidationException(String.format("%s exceeds maximum allowed value", fieldName));
            }
        }
    }

    public static void validatePriorityRange(Integer priority, String fieldName) {
        if (priority != null) {
            if (priority < 1 || priority > 10) {
                throw new ValidationException(String.format("%s must be between 1 and 10", fieldName));
            }
        }
    }

    public static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new ValidationException("Start date must be before or equal to end date");
            }
        }
    }

    public static void validateFutureDate(LocalDate date, String fieldName) {
        if (date != null) {
            if (date.isBefore(LocalDate.now())) {
                throw new ValidationException(String.format("%s must be in the future", fieldName));
            }
        }
    }

    public static void validateEnum(String value, String[] validValues, String fieldName) {
        if (value != null) {
            for (String validValue : validValues) {
                if (validValue.equals(value)) {
                    return;
                }
            }
            throw new ValidationException(
                String.format("Invalid %s value. Must be one of: %s", fieldName, String.join(", ", validValues))
            );
        }
    }

    public static void validateUrl(String url, String fieldName) {
        if (url != null && !url.trim().isEmpty()) {
            try {
                java.net.URI.create(url).toURL();
            } catch (java.net.MalformedURLException | IllegalArgumentException e) {
                throw new ValidationException(String.format("Invalid URL format for %s", fieldName));
            }
        }
    }

    public static void validateCurrencyCode(String currencyCode) {
        if (currencyCode != null && !currencyCode.trim().isEmpty()) {
            if (currencyCode.length() != 3 || !currencyCode.matches("[A-Z]{3}")) {
                throw new ValidationException("Currency code must be a 3-letter ISO code (e.g., USD, EUR, GBP)");
            }
        }
    }

    public static void validateSlug(String slug, String fieldName) {
        if (slug != null && !slug.trim().isEmpty()) {
            if (!slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
                throw new ValidationException(
                    String.format("%s must contain only lowercase letters, numbers, and hyphens", fieldName)
                );
            }
        }
    }

    public static void validateFieldErrors(Map<String, String> errors) {
        if (errors != null && !errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    public static String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    public static String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.trim().toUpperCase();
    }
}