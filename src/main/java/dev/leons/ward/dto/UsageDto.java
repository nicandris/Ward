package dev.leons.ward.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * UsageDto is a values container for presenting server usage
 *
 * @author Rudolf Barbu
 * @version 1.0.1
 */
@Getter
@Setter
@Builder
public class UsageDto {
    /**
     * Processor usage field
     */
    private int processor;

    /**
     * Ram usage field
     */
    private int ram;

    /**
     * Total storage usage field
     */
    private int storage;

    /**
     * Disk usage field
     */
    private List<Integer> disks;
}