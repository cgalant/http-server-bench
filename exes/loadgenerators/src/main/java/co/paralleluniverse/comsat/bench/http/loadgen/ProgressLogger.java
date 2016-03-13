package co.paralleluniverse.comsat.bench.http.loadgen;

import com.pinterest.jbender.events.TimingEvent;
import com.pinterest.jbender.events.recording.Recorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

final class ProgressLogger<Res, Exec extends AutoCloseableRequestExecutor<?, Res>> implements Recorder<Res> {
  private static final Logger log = LoggerFactory.getLogger(ProgressLogger.class);

  private final Timer tp;

  AtomicLong succ = new AtomicLong(0);
  AtomicLong err = new AtomicLong(0);

  AtomicReference<Date>
      start = new AtomicReference<>(null),
      end = new AtomicReference<>(null);

  private long notified = 0;
  private long notifiedNanos = System.nanoTime();
  private long avgDurationNanos = -1;

  private AtomicLong durationsSum = new AtomicLong(0);

  public ProgressLogger(Exec requestExecutor, int total, int cmpi) {
    log.info("Starting progress report");

    tp = new Timer(true);

    tp.schedule (
      new TimerTask() {
        @Override
        public void run() {
          final long succeeded = succ.get();
          final long errored = err.get();

          final long end = System.nanoTime();

          final long finished = succeeded + errored;
          final long finishedRoundedPercent = (long) Math.floor(((double) (succeeded + errored)) / ((double) total) * 100.0D);

          // Notify progress
          final long newFinished = finished - notified;
          final long newTimeNanos = end - notifiedNanos;
          notified = finished;
          notifiedNanos = end;

          final double succeededPercent = Math.round(((double) (succeeded)) / ((double) total) * 100.0D * 100.D) / 100.D;
          final double erroredPercent = Math.round(((double) (errored)) / ((double) total) * 100.0D * 100.D) / 100.D;
          final String avgRPSStr = avgDurationNanos > 0 ? Double.toString(1.0D / (avgDurationNanos) * 1_000_000_000_000L) : "N/A";

          log.info((succeeded + errored) + "/" + finishedRoundedPercent + "% (" + succeeded + "/" + succeededPercent + "% OK + " + errored + "/" + erroredPercent + "% KO) / " + total + " (+" + newFinished + " reqs in " + newTimeNanos + " nanos, " + avgRPSStr + " avg rps, concurrency = " + requestExecutor.getCurrentConcurrency() + ", max = " + requestExecutor.getMaxConcurrency() + ")");

          if (succeeded + errored == total)
            tp.cancel();
        }
      },
      cmpi,
      cmpi
    );
  }

  @Override
  public final void record(final TimingEvent<Res> timingEvent) {
    final Date now = new Date();
    start.compareAndSet(null, now);
    end.set(now);

    durationsSum.addAndGet(timingEvent.durationNanos);

    final long succeeded = succ.get();
    final long errored = err.get();
    final long total = succeeded + errored;

    long l = durationsSum.get();
    avgDurationNanos = total > 0 && l > 0 ? l / total : avgDurationNanos;

    if (timingEvent.isSuccess)
      succ.incrementAndGet();
    else
      err.incrementAndGet();
  }

  public final void stopProgressLog() {
    if (tp != null)
      tp.cancel();
  }
}
