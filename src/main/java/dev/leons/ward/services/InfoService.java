package dev.leons.ward.services;

import dev.leons.ward.Ward;
import dev.leons.ward.dto.InfoDto;
import dev.leons.ward.dto.MachineDto;
import dev.leons.ward.dto.ProcessorDto;
import dev.leons.ward.dto.StorageDto;
import dev.leons.ward.dto.DiskDto;
import dev.leons.ward.components.UtilitiesComponent;
import dev.leons.ward.exceptions.ApplicationNotConfiguredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.PhysicalMemory;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * InfoService provides various information about machine, such as processor name, core count, Ram amount, etc.
 *
 * @author Rudolf Barbu
 * @version 1.0.2
 */
@Service
public class InfoService {
    /**
     * Autowired SystemInfo object
     * Used for getting machine information
     */
    @Autowired
    private SystemInfo systemInfo;

    /**
     * Autowired UtilitiesComponent object
     * Used for various utility functions
     */
    @Autowired
    private UtilitiesComponent utilitiesComponent;

    /**
     * Converts frequency to most readable format
     *
     * @param hertzArray raw frequency array values in hertz for each logical processor
     * @return String with formatted frequency and postfix
     */
    private String getConvertedFrequency(final long[] hertzArray) {
        long totalFrequency = Arrays.stream(hertzArray).sum();
        long hertz = totalFrequency / hertzArray.length;

        if ((hertz / 1E+6) > 999) {
            return (Math.round((hertz / 1E+9) * 10.0) / 10.0) + " GHz";
        } else {
            return Math.round(hertz / 1E+6) + " MHz";
        }
    }

    /**
     * Converts capacity to most readable format
     *
     * @param bits raw capacity value in bits
     * @return String with formatted capacity and postfix
     */
    private static String getConvertedCapacity(final long bits) {
        if ((bits / 1.049E+6) > 999) {
            if ((bits / 1.074E+9) > 999) {
                return (Math.round((bits / 1.1E+12) * 10.0) / 10.0) + " TiB";
            } else {
                return Math.round(bits / 1.074E+9) + " GiB";
            }
        } else {
            return Math.round(bits / 1.049E+6) + " MiB";
        }
    }

    /**
     * Gets processor information
     *
     * @return ProcessorDto with filled fields
     */
    private ProcessorDto getProcessor() {
        ProcessorDto processorDto = new ProcessorDto();

        CentralProcessor centralProcessor = systemInfo.getHardware().getProcessor();

        String name = centralProcessor.getProcessorIdentifier().getName();
        if (name.contains("@")) {
            name = name.substring(0, name.indexOf('@') - 1);
        }
        processorDto.setName(name.trim());

        int coreCount = centralProcessor.getLogicalProcessorCount();
        processorDto.setCoreCount(coreCount + ((coreCount > 1) ? " Cores" : " Core"));
        processorDto.setClockSpeed(getConvertedFrequency(centralProcessor.getCurrentFreq()));

        String bitDepthPrefix = centralProcessor.getProcessorIdentifier().isCpu64bit() ? "64" : "32";
        processorDto.setBitDepth(bitDepthPrefix + "-bit");

        return processorDto;
    }

    /**
     * Gets machine information
     *
     * @return MachineDto with filled fields
     */
    private MachineDto getMachine() {
        MachineDto machineDto = new MachineDto();

        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        OperatingSystem.OSVersionInfo osVersionInfo = systemInfo.getOperatingSystem().getVersionInfo();
        GlobalMemory globalMemory = systemInfo.getHardware().getMemory();

        machineDto.setOperatingSystem(operatingSystem.getFamily() + " " + osVersionInfo.getVersion() + ", " + osVersionInfo.getCodeName());
        machineDto.setTotalRam(getConvertedCapacity(globalMemory.getTotal()) + " Ram");

        Optional<PhysicalMemory> physicalMemoryOptional = globalMemory.getPhysicalMemory().stream().findFirst();
        if (physicalMemoryOptional.isPresent()) {
            machineDto.setRamTypeOrOSBitDepth(physicalMemoryOptional.get().getMemoryType());
        } else {
            machineDto.setRamTypeOrOSBitDepth(operatingSystem.getBitness() + "-bit");
        }

        int processCount = operatingSystem.getProcessCount();
        machineDto.setProcCount(processCount + ((processCount > 1) ? " Procs" : " Proc"));

        return machineDto;
    }

    /**
     * Gets storage information
     *
     * @return StorageDto with filled fields
     */
    private StorageDto getStorage() {
        final GlobalMemory globalMemory = systemInfo.getHardware().getMemory();
        final List<OSFileStore> fileStores = systemInfo.getOperatingSystem().getFileSystem().getFileStores();

        final long total = fileStores.stream().mapToLong(OSFileStore::getTotalSpace).sum();
        final List<DiskDto> disks = getDisks(fileStores);
        final String diskCount = disks.size() + ((disks.size() > 1) ? " Disks" : " Disk");
        final String totalCapacity = getConvertedCapacity(total) + " Total";
        final String swap = getConvertedCapacity(globalMemory.getVirtualMemory().getSwapTotal()) + " Swap";
        return StorageDto.builder()
                .disks(disks)
                .diskCount(diskCount)
                .total(totalCapacity)
                .swapAmount(swap)
                .build();
    }

    /**
     * Used to deliver dto to corresponding controller
     *
     * @return InfoDto filled with server info
     */
    public InfoDto getInfo() throws ApplicationNotConfiguredException {
        if (!Ward.isFirstLaunch()) {
            InfoDto infoDto = new InfoDto();

            infoDto.setProcessor(getProcessor());
            infoDto.setMachine(getMachine());
            infoDto.setStorage(getStorage());

            return infoDto;
        } else {
            throw new ApplicationNotConfiguredException();
        }
    }

    private static List<DiskDto> getDisks(final List<OSFileStore> fileStores) {
        return fileStores.stream().map(fileStore -> DiskDto.builder()
                .size(getConvertedCapacity(fileStore.getTotalSpace()))
                .free(getConvertedCapacity(fileStore.getFreeSpace()))
                .name(fileStore.getName())
                .mount(fileStore.getMount())
                .build()).toList();
    }
}