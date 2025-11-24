package com.sentinovo.carbuildervin.services.parts;

import com.sentinovo.carbuildervin.entities.parts.PartTier;
import com.sentinovo.carbuildervin.exception.DuplicateResourceException;
import com.sentinovo.carbuildervin.exception.InvalidStateException;
import com.sentinovo.carbuildervin.exception.ResourceNotFoundException;
import com.sentinovo.carbuildervin.repository.parts.PartTierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PartTierService {

    private final PartTierRepository partTierRepository;

    @Transactional(readOnly = true)
    public PartTier findByCode(String code) {
        return partTierRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("PartTier", code));
    }

    @Transactional(readOnly = true)
    public Optional<PartTier> findByCodeOptional(String code) {
        return partTierRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public List<PartTier> findAllTiers() {
        return partTierRepository.findAllOrderByRank();
    }

    @Transactional(readOnly = true)
    public List<PartTier> findTiersByRank(Integer rank) {
        return partTierRepository.findByRank(rank);
    }

    @Transactional(readOnly = true)
    public List<PartTier> findTiersAboveRank(Integer minRank) {
        return partTierRepository.findByRankGreaterThanEqual(minRank);
    }

    @Transactional(readOnly = true)
    public List<PartTier> findTiersBelowRank(Integer maxRank) {
        return partTierRepository.findByRankLessThanEqual(maxRank);
    }

    @Transactional(readOnly = true)
    public List<PartTier> findTiersInRankRange(Integer minRank, Integer maxRank) {
        return partTierRepository.findByRankBetween(minRank, maxRank);
    }

    @Transactional(readOnly = true)
    public List<PartTier> searchTiers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAllTiers();
        }
        return partTierRepository.searchByCodeLabelOrDescription(searchTerm.trim());
    }

    @Transactional(readOnly = true)
    public long countPartsInTier(String tierCode) {
        return partTierRepository.countPartsByTierCode(tierCode);
    }

    @Transactional(readOnly = true)
    public long countSubPartsInTier(String tierCode) {
        return partTierRepository.countSubPartsByTierCode(tierCode);
    }

    @Transactional(readOnly = true)
    public long countTotalItemsInTier(String tierCode) {
        return countPartsInTier(tierCode) + countSubPartsInTier(tierCode);
    }

    public PartTier createTier(String code, String label, Integer rank, String description) {
        log.info("Creating new part tier with code: {} and rank: {}", code, rank);
        
        validateTierCreation(code, rank);
        
        PartTier tier = PartTier.builder()
                .code(code.toUpperCase())
                .label(label)
                .rank(rank)
                .description(description)
                .build();

        PartTier savedTier = partTierRepository.save(tier);
        log.info("Successfully created part tier with code: {}", savedTier.getCode());
        return savedTier;
    }

    public PartTier updateTier(String code, String label, Integer rank, String description) {
        log.info("Updating part tier with code: {}", code);
        
        PartTier tier = findByCode(code);
        
        if (label != null) tier.setLabel(label);
        if (description != null) tier.setDescription(description);
        
        if (rank != null && !rank.equals(tier.getRank())) {
            validateRankUniquenessForUpdate(rank, code);
            tier.setRank(rank);
        }

        PartTier savedTier = partTierRepository.save(tier);
        log.info("Successfully updated part tier with code: {}", savedTier.getCode());
        return savedTier;
    }

    public PartTier updateTierRank(String code, Integer rank) {
        log.info("Updating rank for part tier with code: {} to {}", code, rank);
        
        PartTier tier = findByCode(code);
        validateRankUniquenessForUpdate(rank, code);
        tier.setRank(rank);
        
        PartTier savedTier = partTierRepository.save(tier);
        log.info("Successfully updated rank for part tier with code: {}", savedTier.getCode());
        return savedTier;
    }

    public void deleteTier(String code) {
        log.info("Deleting part tier with code: {}", code);
        
        PartTier tier = findByCode(code);
        
        long totalItems = countTotalItemsInTier(code);
        if (totalItems > 0) {
            throw new InvalidStateException(
                String.format("Cannot delete tier '%s' - it is used by %d part(s)", 
                    tier.getLabel(), totalItems)
            );
        }
        
        partTierRepository.delete(tier);
        log.info("Successfully deleted part tier with code: {}", code);
    }

    @Transactional(readOnly = true)
    public boolean isTierCodeAvailable(String code) {
        return !partTierRepository.existsByCode(code.toUpperCase());
    }

    @Transactional(readOnly = true)
    public boolean isRankAvailable(Integer rank) {
        return partTierRepository.findByRank(rank).isEmpty();
    }

    @Transactional(readOnly = true)
    public Integer getNextAvailableRank() {
        Integer maxRank = partTierRepository.findMaxRank();
        return (maxRank != null) ? maxRank + 1 : 1;
    }

    public void ensureDefaultTiers() {
        log.info("Ensuring default part tiers exist");
        
        String[][] defaultTiers = {
            {"ECONOMY", "Economy", "1", "Budget-friendly options with basic functionality"},
            {"OEM", "OEM", "2", "Original Equipment Manufacturer parts"},
            {"OEM_PLUS", "OEM+", "3", "Enhanced OEM-equivalent parts"},
            {"PERFORMANCE", "Performance", "4", "Performance-oriented upgrades"},
            {"SPORT", "Sport", "5", "Sport-tuned components"},
            {"TRACK", "Track", "6", "Track-focused high-performance parts"},
            {"RACE", "Race", "7", "Professional racing components"},
            {"PREMIUM", "Premium", "8", "High-end premium parts"},
            {"EXOTIC", "Exotic", "9", "Exotic and specialty components"},
            {"CUSTOM", "Custom", "10", "Custom and bespoke parts"}
        };
        
        for (String[] tierData : defaultTiers) {
            String code = tierData[0];
            Integer rank = Integer.parseInt(tierData[2]);
            
            // Check if tier exists by code OR rank
            Optional<PartTier> existingByCode = findByCodeOptional(code);
            List<PartTier> existingByRank = partTierRepository.findByRank(rank);
            
            if (existingByCode.isEmpty() && existingByRank.isEmpty()) {
                createTier(code, tierData[1], rank, tierData[3]);
            } else if (existingByCode.isPresent()) {
                log.debug("Part tier with code '{}' already exists, skipping", code);
            } else if (!existingByRank.isEmpty()) {
                log.debug("Part tier with rank '{}' already exists, skipping", rank);
            }
        }
        
        log.info("Default part tiers ensured");
    }

    private void validateTierCreation(String code, Integer rank) {
        if (partTierRepository.existsByCode(code.toUpperCase())) {
            throw new DuplicateResourceException("PartTier", "code", code);
        }
        
        if (!partTierRepository.findByRank(rank).isEmpty()) {
            throw new DuplicateResourceException("PartTier", "rank", rank.toString());
        }
    }

    private void validateRankUniquenessForUpdate(Integer rank, String excludeCode) {
        if (partTierRepository.existsByRankAndCodeNot(rank, excludeCode.toUpperCase())) {
            throw new DuplicateResourceException("PartTier", "rank", rank.toString());
        }
    }
}