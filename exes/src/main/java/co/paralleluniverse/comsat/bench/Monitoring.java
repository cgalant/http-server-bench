package co.paralleluniverse.comsat.bench;

import javax.management.*;
import java.lang.management.*;
import java.util.List;

public final class Monitoring {
    private static final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private static final OperatingSystemMXBean osMX = ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean memMX = ManagementFactory.getMemoryMXBean();
    private static final CompilationMXBean compMX = ManagementFactory.getCompilationMXBean();
    private static final ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();
    private static final List<GarbageCollectorMXBean> gcMXs = ManagementFactory.getGarbageCollectorMXBeans();
    // private static final List<MemoryPoolMXBean> poolMXs = ManagementFactory.getMemoryPoolMXBeans();
    // private static final RuntimeMXBean runtimeMX = ManagementFactory.getRuntimeMXBean();
    // private static final Runtime r = Runtime.getRuntime();
    private static final ObjectName name;

    static {
        try {
            name = ObjectName.getInstance("java.lang:type=OperatingSystem");
        } catch (final MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean missedGCs;
    private static long maxGCTimeMS;
    private static long samples = 0L;
    private static long totalGCs = 0L;
    private static long totalGCTimeMS = 0L;
    private static long maxDaemonThreads = 0L;
    private static double maxCpu = 0.0D, totCpu = 0.0D;
    private static long
        maxHeapMem = 0L, maxNonHeapMem = 0L,
        totHeapMem = 0L, totNonHeapMem = 0L, totThreads = 0L, totDaemonThreads = 0L;

    public static final class SysStats {
        public boolean missedGCs;
        public double
            avgGCTimeMS, avgOSLoad, cpu, maxCpu, avgCpu, avgThreads, avgDaemonThreads,
            avgHeapMem, avgNonHeapMem;
        public long
            samples, totalGCs, totalGCTimeMS, maxGCTimeMS, threads, maxThreads, daemonThreads, maxDaemonThreads,
            heapMem, maxHeapMem, nonHeapMem, maxNonHeapMem, totalCompilationTime;
    }

    public static SysStats sampleSys() {
        final SysStats ret = new SysStats();
        ret.samples = ++samples;
        final AttributeList list;
        try {
            ret.totalCompilationTime = compMX.getTotalCompilationTime();
            ret.avgOSLoad = osMX.getSystemLoadAverage();

            final long heapMem = memMX.getHeapMemoryUsage().getUsed();
            ret.heapMem = heapMem;
            ret.maxHeapMem = maxHeapMem = Math.max(maxHeapMem, heapMem);
            ret.avgHeapMem = ((double) totHeapMem) / samples;
            totHeapMem += heapMem;

            final long nonHeapMem = memMX.getNonHeapMemoryUsage().getUsed();
            ret.nonHeapMem = nonHeapMem;
            ret.maxNonHeapMem = maxNonHeapMem = Math.max(maxNonHeapMem, nonHeapMem);
            ret.avgNonHeapMem = ((double) totNonHeapMem) / samples;
            totNonHeapMem += nonHeapMem;

            final long daemonThreads = threadMX.getDaemonThreadCount();
            ret.daemonThreads = daemonThreads;
            ret.maxDaemonThreads = maxDaemonThreads = Math.max(maxDaemonThreads, daemonThreads);
            ret.avgDaemonThreads = ((double) totDaemonThreads) / samples;
            totDaemonThreads += daemonThreads;

            ret.threads = threadMX.getThreadCount();
            ret.maxThreads = threadMX.getPeakThreadCount();
            ret.avgThreads = ((double) totThreads) / samples;
            totThreads += ret.threads;

            list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});
            final Attribute att = (Attribute) list.get(0);
            final double cpu = (int) ((double) att.getValue() * 1_000.0D) / 10.0D;
            ret.cpu = cpu;
            ret.maxCpu = maxCpu = Math.max(maxCpu, cpu);
            ret.avgCpu = totCpu / samples;
            totCpu += cpu;

            final long totalGCsBak = totalGCs;
            final long totalGCTimeMSBak = totalGCTimeMS;
            long totalGarbageCollections = 0;
            long totalGarbageCollectionTime = 0;
            for (final GarbageCollectorMXBean gc : gcMXs) {
                totalGarbageCollections += gc.getCollectionCount();
                totalGarbageCollectionTime += gc.getCollectionTime();
            }
            ret.totalGCs = totalGCs = totalGarbageCollections;
            ret.totalGCTimeMS = totalGCTimeMS = totalGarbageCollectionTime;
            ret.avgGCTimeMS = Math.round(totalGarbageCollectionTime / totalGarbageCollections * 100.D) / 100.D;
            ret.missedGCs = missedGCs =
                missedGCs || (totalGCsBak != 0 && totalGCsBak != totalGCs && totalGCsBak + 1 < totalGCs);
            ret.maxGCTimeMS = maxGCTimeMS = Math.max(maxGCTimeMS, totalGCTimeMS - totalGCTimeMSBak);
        } catch (final ReflectionException | InstanceNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void resetSampleSys() {
        missedGCs = false;
        maxGCTimeMS = 0L;
        samples = 0L;
        totalGCs = 0L;
        totalGCTimeMS = 0L;
        maxDaemonThreads = 0L;
        maxCpu = 0.0D;
        maxHeapMem = 0L;
        maxNonHeapMem = 0L;
        totCpu = 0.0D;
        totHeapMem = 0L;
        totNonHeapMem = 0L;
        totThreads = 0L;
        totDaemonThreads = 0L;
    }

    private Monitoring() {}
}
