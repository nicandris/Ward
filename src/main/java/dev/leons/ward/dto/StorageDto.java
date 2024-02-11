package dev.leons.ward.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * StorageDto is a values container for presenting storage principal information
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
public class StorageDto {
    /**
     * Amount of total installed storage field
     */
    private String total;

    /**
     * Disk count field
     */
    private String diskCount;

    /**
     * Total amount of virtual memory (Swap on Linux) field
     */
    private String swapAmount;

    /**
     * Disks field
     */
    private List<DiskDto> disks;
}