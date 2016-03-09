package co.paralleluniverse.comsat.bench;

import javax.management.*;
import java.lang.management.*;
import java.util.List;
import java.util.Optional;

public final class Monitoring {
    private static final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private static final OperatingSystemMXBean osMX = ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean memMX = ManagementFactory.getMemoryMXBean();
    private static final CompilationMXBean compMX = ManagementFactory.getCompilationMXBean();
    private static final ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();
    private static final List<GarbageCollectorMXBean> gcMXs = ManagementFactory.getGarbageCollectorMXBeans();
    // private static final List<MemoryPoolMXBean> poolMXs = ManagementFactory.getMemoryPoolMXBeans();
    // private static final RuntimeMXBean runtimeMX = ManagementFactory.getRuntimeMXBean();
    private static final Runtime r = Runtime.getRuntime();
    private static final ObjectName name;

    static {
        try {
            name = ObjectName.getInstance("java.lang:type=OperatingSystem");
        } catch (final MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean missedGCs;
    private static Optional<Long> maxGCTimeMS = Optional.empty();
    private static long samples = 0L;
    private static long totalGCs = 0L;
    private static long totalGCTimeMS = 0L;
    private static long maxDaemonThreads = 0L;
    private static double
        maxCpu = 0.0D, maxMemMB = 0.0D, maxHeapMemMB = 0.0D, maxNonHeapMemMB = 0.0D;
    private static double
        totCpu = 0.0D, totMemMB = 0.0D, totHeapMemMB = 0.0D, totNonHeapMemMB = 0.0D, totThreads = 0L, totDaemonThreads = 0L;

    public static final class SysStats {
        public long samples;
        public long totalCompilationTime;
        public double avgOSLoad;

        public Optional<Long> maxGCTimeMS = Optional.empty();
        public long totalGCs, totalGCTimeMS;
        public double avgGCTimeMS;
        public double cpu, maxCpu, avgCpu;
        public long threads, maxThreads;
        public double avgThreads;
        public long daemonThreads, maxDaemonThreads;
        public double avgDaemonThreads;
        public double memMB, maxMemMB, avgMemMB;
        public double heapMemMB, maxHeapMemMB, avgHeapMemMB;
        public double nonHeapMemMB, maxNonHeapMemMB, avgNonHeapMemMB;
    }

    public static SysStats sampleSys() {
        final SysStats ret = new SysStats();
        ret.samples = ++samples;
        final AttributeList list;
        try {
            ret.totalCompilationTime = compMX.getTotalCompilationTime();
            ret.avgOSLoad = osMX.getSystemLoadAverage();

            final double memMB = (r.totalMemory() - r.freeMemory()) / (1_024.0D * 1_024.0D);
            totMemMB += memMB;
            maxMemMB = Math.max(maxMemMB, memMB);
            ret.memMB = Math.round(memMB * 100.0D) / 100.0D;
            ret.maxMemMB = Math.round(maxMemMB * 100.0D) / 100.0D;
            ret.avgMemMB = Math.round(totMemMB / samples * 100.0D) / 100.0D;

            final double heapMemMB = memMX.getHeapMemoryUsage().getUsed() / (1_024.0D * 1_024.0D);
            totHeapMemMB += heapMemMB;
            maxHeapMemMB = Math.max(maxHeapMemMB, heapMemMB);
            ret.heapMemMB = Math.round(heapMemMB * 100.0D) / 100.0D;
            ret.maxHeapMemMB = Math.round(maxHeapMemMB * 100.0D) / 100.0D;
            ret.avgHeapMemMB = Math.round(totHeapMemMB / samples * 100.0D) / 100.0D;

            final double nonHeapMemMB = memMX.getNonHeapMemoryUsage().getUsed() / (1_024.0D * 1_024.0D);
            totNonHeapMemMB += nonHeapMemMB;
            maxNonHeapMemMB = Math.max(maxNonHeapMemMB, nonHeapMemMB);
            ret.nonHeapMemMB = Math.round(nonHeapMemMB * 100.0D) / 100.0D;
            ret.maxNonHeapMemMB = Math.round(maxNonHeapMemMB * 100.0D) / 100.0D;
            ret.avgNonHeapMemMB = Math.round(totNonHeapMemMB / samples * 100.0D) / 100.0D;

            ret.threads = threadMX.getThreadCount();
            totThreads += ret.threads;
            ret.maxThreads = threadMX.getPeakThreadCount();
            ret.avgThreads = Math.round(totThreads / samples * 100.0D) / 100.0D;

            final long daemonThreads = threadMX.getDaemonThreadCount();
            totDaemonThreads += daemonThreads;
            maxDaemonThreads = Math.max(maxDaemonThreads, daemonThreads);
            ret.daemonThreads = daemonThreads;
            ret.maxDaemonThreads = maxDaemonThreads;
            ret.avgDaemonThreads = Math.round(totDaemonThreads / samples * 100.0D) / 100.0D;

            list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});
            final Attribute att = (Attribute) list.get(0);
            final double cpu = (int) ((double) att.getValue() * 1_000.0D) / 10.0D;
            totCpu += cpu;
            maxCpu = Math.max(maxCpu, cpu);
            ret.cpu = Math.round(cpu * 100.0D) / 100.0D;
            ret.maxCpu = Math.round(maxCpu * 100.0D) / 100.0D;
            ret.avgCpu = Math.round(totCpu / samples * 100.0D) / 100.0D;

            final long totalGCsBak = totalGCs;
            final long totalGCTimeMSBak = totalGCTimeMS;
            long totalGarbageCollections = 0;
            long totalGarbageCollectionTime = 0;
            for (final GarbageCollectorMXBean gc : gcMXs) {
                totalGarbageCollections += gc.getCollectionCount();
                totalGarbageCollectionTime += gc.getCollectionTime();
            }
            totalGCs = ret.totalGCs = totalGarbageCollections;
            totalGCTimeMS = ret.totalGCTimeMS = totalGarbageCollectionTime;
            ret.avgGCTimeMS = Math.round(totalGarbageCollectionTime / totalGarbageCollections * 100.D) / 100.D;
            if (totalGCsBak != totalGCs && totalGCsBak < totalGCs + 1 && totalGCsBak != 0)
                missedGCs = true;
            if (!missedGCs) {
                long lastGCTime = totalGCTimeMS - totalGCTimeMSBak;
                maxGCTimeMS = ret.maxGCTimeMS = Optional.of(Math.max(maxGCTimeMS.orElse(0L), lastGCTime));
            }
        } catch (final ReflectionException | InstanceNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void resetSampleSys() {
        missedGCs = false;
        maxGCTimeMS = Optional.empty();
        samples = 0L;
        totalGCs = 0L;
        totalGCTimeMS = 0L;
        maxDaemonThreads = 0L;
        maxCpu = 0.0D;
        maxMemMB = 0.0D;
        maxHeapMemMB = 0.0D;
        maxNonHeapMemMB = 0.0D;
        totCpu = 0.0D;
        totMemMB = 0.0D;
        totHeapMemMB = 0.0D;
        totNonHeapMemMB = 0.0D;
        totThreads = 0L;
        totDaemonThreads = 0L;
    }

    private Monitoring() {}
}
