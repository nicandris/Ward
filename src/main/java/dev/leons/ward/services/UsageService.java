package dev.leons.ward.services;

import dev.leons.ward.Ward;
import dev.leons.ward.dto.UsageDto;
import dev.leons.ward.exceptions.ApplicationNotConfiguredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.util.Util;

import java.util.Arrays;
import java.util.List;

/**
 * UsageService provides principal information of processor, RAM and storage usage to rest controller
 *
 * @author Rudolf Barbu
 * @version 1.0.3
 */
@Service
public class UsageService {
    /**
     * Autowired SystemInfo object
     * Used for getting usage information
     */
    @Autowired
    private SystemInfo systemInfo;

    /**
     * Used to deliver dto to corresponding controller
     *
     * @return ResponseEntityWrapperAsset filled with usageDto
     */
    public UsageDto getUsage() throws ApplicationNotConfiguredException {
        if (!Ward.isFirstLaunch()) {
            return UsageDto.builder()
                    .processor(getProcessor())
                    .ram(getRam())
                    .storage(getStorageTotal())
                    .disks(getStorage())
                    .build();
        } else {
            throw new ApplicationNotConfiguredException();
        }
    }

    /**
     * Gets processor usage
     *
     * @return int that display processor usage
     */
    private int getProcessor() {
        final CentralProcessor centralProcessor = systemInfo.getHardware().getProcessor();

        final long[] prevTicksArray = centralProcessor.getSystemCpuLoadTicks();
        final long prevTotalTicks = Arrays.stream(prevTicksArray).sum();
        final long prevIdleTicks = prevTicksArray[CentralProcessor.TickType.IDLE.getIndex()];

        Util.sleep(1000);

        final long[] currTicksArray = centralProcessor.getSystemCpuLoadTicks();
        final long currTotalTicks = Arrays.stream(currTicksArray).sum();
        final long currIdleTicks = currTicksArray[CentralProcessor.TickType.IDLE.getIndex()];

        return (int) Math.round((1 - ((double) (currIdleTicks - prevIdleTicks)) / ((double) (currTotalTicks - prevTotalTicks))) * 100);
    }

    /**
     * Gets ram usage
     *
     * @return int that display ram usage
     */
    private int getRam() {
        final GlobalMemory globalMemory = systemInfo.getHardware().getMemory();

        final long totalMemory = globalMemory.getTotal();
        final long availableMemory = globalMemory.getAvailable();

        return (int) Math.round(100 - (((double) availableMemory / totalMemory) * 100));
    }

    /**
     * Gets storage usage
     *
     * @return int that display storage usage
     */
    private List<Integer> getStorage() {
        final List<OSFileStore> fileStores = systemInfo.getOperatingSystem().getFileSystem().getFileStores();
        return fileStores.stream().map(fileStore -> (int) Math.round(((double) (fileStore.getTotalSpace() - fileStore.getFreeSpace()) / fileStore.getTotalSpace()) * 100)).toList();
    }

    /**
     * Gets total storage usage
     *
     * @return int that display storage usage
     */
    private int getStorageTotal() {
        final FileSystem fileSystem = systemInfo.getOperatingSystem().getFileSystem();

        final long totalStorage = fileSystem.getFileStores().stream().mapToLong(OSFileStore::getTotalSpace).sum();
        final long freeStorage = fileSystem.getFileStores().stream().mapToLong(OSFileStore::getFreeSpace).sum();

        return (int) Math.round(((double) (totalStorage - freeStorage) / totalStorage) * 100);
    }
}