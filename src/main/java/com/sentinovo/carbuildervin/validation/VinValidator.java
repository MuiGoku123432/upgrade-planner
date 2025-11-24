package com.sentinovo.carbuildervin.validation;

import java.util.regex.Pattern;

public class VinValidator {

    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");
    
    private static final int[] VIN_WEIGHTS = {8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final String VIN_CHECK_DIGITS = "0123456789X";

    public static boolean isValidVin(String vin) {
        if (vin == null || vin.length() != 17) {
            return false;
        }
        
        String normalizedVin = vin.trim().toUpperCase();
        
        if (!VIN_PATTERN.matcher(normalizedVin).matches()) {
            return false;
        }
        
        return validateCheckDigit(normalizedVin);
    }

    public static String normalizeVin(String vin) {
        if (vin == null) {
            return null;
        }
        return vin.trim().toUpperCase();
    }

    private static boolean validateCheckDigit(String vin) {
        int sum = 0;
        
        for (int i = 0; i < 17; i++) {
            if (i == 8) continue; // Skip check digit position
            
            char c = vin.charAt(i);
            int value = getCharacterValue(c);
            sum += value * VIN_WEIGHTS[i];
        }
        
        int checkDigit = sum % 11;
        char expectedCheckDigit = VIN_CHECK_DIGITS.charAt(checkDigit);
        
        return vin.charAt(8) == expectedCheckDigit;
    }

    private static int getCharacterValue(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        
        // VIN letter values
        switch (c) {
            case 'A': case 'J': return 1;
            case 'B': case 'K': case 'S': return 2;
            case 'C': case 'L': case 'T': return 3;
            case 'D': case 'M': case 'U': return 4;
            case 'E': case 'N': case 'V': return 5;
            case 'F': case 'W': return 6;
            case 'G': case 'P': case 'X': return 7;
            case 'H': case 'Y': return 8;
            case 'R': case 'Z': return 9;
            default: return 0;
        }
    }
}