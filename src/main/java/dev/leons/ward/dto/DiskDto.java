package dev.leons.ward.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DiskDto {
    /**
     * Disk name field
     */
    private String name;

    /**
     * Disk mount field
     */
    private String mount;

    /**
     * Disk size field
     */
    private String size;

    /**
     * Disk free size field
     */
    private String free;
}
