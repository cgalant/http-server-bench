Build all with `gradle fatCapsule`.

Load generator env vars:

``` bash
# LOADGEN SYSTEM

export JVMARGS="-server -XX:+AggressiveOpts -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms12G -Xmx12G -XX:+UseG1GC -XX:MaxGCPauseMillis=10"

export SERVER=x.x.x.x
```

Script snippet for runs:

``` bash
export SERVER_TECH=jetty-handler-async-dispatch ; export PORT=8000
# export SERVER_TECH=jetty-handler-async-timer-complete ; export PORT=8001
# export SERVER_TECH=jetty-handler-sync ; export PORT=8002

# export SERVER_TECH=servlet-async-dispatch-jetty ; export PORT=8003
# export SERVER_TECH=servlet-async-dispatch-tomcat ; export PORT=8004
# export SERVER_TECH=servlet-async-dispatch-undertow ; export PORT=8005

# export SERVER_TECH=servlet-async-fjp-complete-jetty ; export PORT=8006
# export SERVER_TECH=servlet-async-fjp-complete-tomcat ; export PORT=8007
# export SERVER_TECH=servlet-async-fjp-complete-undertow ; export PORT=8008

# export SERVER_TECH=servlet-sync-comsat-jetty ; export PORT=8009
# export SERVER_TECH=servlet-sync-comsat-tomcat ; export PORT=8010
# export SERVER_TECH=servlet-sync-comsat-undertow ; export PORT=8011

# export SERVER_TECH=servlet-sync-jetty ; export PORT=8012
# export SERVER_TECH=servlet-sync-tomcat ; export PORT=8013
# export SERVER_TECH=servlet-sync-undertow ; export PORT=8014

# export SERVER_TECH=spark-handler-sync-simple ; export PORT=8015

# export SERVER_TECH=undertow-handler-async-same-thread ; export PORT=8016
# export SERVER_TECH=undertow-handler-async-same-thread-queue ; export PORT=8017

# export SERVER_TECH=undertow-handler-sync-simple ; export PORT=8018
# export SERVER_TECH=undertow-handler-sync-techempower ; export PORT=8019

# export SERVER_TECH=webactor-native-netty-single ; export PORT=8020
# export SERVER_TECH=webactor-native-netty-multiple ; export PORT=8022

# export SERVER_TECH=webactor-native-undertow-single ; export PORT=8021
# export SERVER_TECH=webactor-native-undertow-multiple ; export PORT=8023

# export SERVER_TECH=webactor-servlet-per-session-jetty ; export PORT=8024
# export SERVER_TECH=webactor-servlet-per-session-tomcat ; export PORT=8025
# export SERVER_TECH=webactor-servlet-per-session-undertow ; export PORT=8026

# PER TECH


# LOADTARGET: concurrency
export DELAY=3600000
export BENCH_NAME=concurrency
# export JVMARGS="-agentlib:jdwp=transport=dt_socket,address=localhost:5005,server=y,suspend=y -Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:-UseGCOverheadLimit -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms1G -Xmx1G -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=${SERVER_TECH}.${BENCH_NAME}.jfr"
export JVMARGS="-Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:-UseGCOverheadLimit -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms1G -Xmx1G -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=${SERVER_TECH}.${BENCH_NAME}.jfr"
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar ${SERVER_TECH}/build/libs/${SERVER_TECH}-fatcap.jar -d ${DELAY} > ${SERVER_TECH}.${BENCH_NAME}.slog 2>&1 &
tail -f ${SERVER_TECH}.${BENCH_NAME}.slog # CTRL-C

# LOADGEN: concurrency
export CLIENT_TECH=jbender-apache-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -c 54000 -n 54000 -rbs 1 -ebs 1 -cmpi 250 -w 0 > ${SERVER_TECH}.concurrency.clog 2>&1 &
tail -f ${SERVER_TECH}.concurrency.clog # CTRL-C


# LOADTARGET: rate1k1000ms
export DELAY=1000
export BENCH_NAME=rate1k1000ms
# export JVMARGS="-agentlib:jdwp=transport=dt_socket,address=localhost:5005,server=y,suspend=y -Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:-UseGCOverheadLimit -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms1G -Xmx1G -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=${SERVER_TECH}.${BENCH_NAME}.jfr"
export JVMARGS="-Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:-UseGCOverheadLimit -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms1G -Xmx1G -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=${SERVER_TECH}.${BENCH_NAME}.jfr"
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar ${SERVER_TECH}/build/libs/${SERVER_TECH}-fatcap.jar -d ${DELAY} > ${SERVER_TECH}.${BENCH_NAME}.slog 2>&1 &
tail -f ${SERVER_TECH}.${BENCH_NAME}.slog # CTRL-C

# LOADGEN: rate1k1000ms warmup
export CLIENT_TECH=jbender-okhttp-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -c 100000 -n 1000 -w 0 -cmpi 250
# rate1k1000ms
export CLIENT_TECH=jbender-apache-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -c 100000 -r 1000 -cmpi 250 -w 0 > ${SERVER_TECH}.rate1k1000ms.clog 2>&1 &
tail -f ${SERVER_TECH}.rate1k1000ms.clog # CTRL-C


# LOADTARGET: rate10k100ms
export DELAY=100
export BENCH_NAME=rate10k100ms
# export JVMARGS="-agentlib:jdwp=transport=dt_socket,address=localhost:5005,server=y,suspend=y -Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:-UseGCOverheadLimit -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms1G -Xmx1G -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=${SERVER_TECH}.${BENCH_NAME}.jfr"
export JVMARGS="-Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:-UseGCOverheadLimit -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms1G -Xmx1G -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=${SERVER_TECH}.${BENCH_NAME}.jfr"
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar ${SERVER_TECH}/build/libs/${SERVER_TECH}-fatcap.jar -d ${DELAY} > ${SERVER_TECH}.${BENCH_NAME}.slog 2>&1 &
tail -f ${SERVER_TECH}.${BENCH_NAME}.slog # CTRL-C

# LOADGEN: rate10k100ms warmup
export CLIENT_TECH=jbender-okhttp-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -c 100000 -n 1000 -w 0 -cmpi 250
# rate10k100ms
export CLIENT_TECH=jbender-okhttp-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -c 100000 -r 10000 -cmpi 250 -w 0 > ${SERVER_TECH}.rate10k100ms.clog 2>&1 &
tail -f ${SERVER_TECH}.rate10k100ms.clog # CTRL-C


# LOADTARGET: rate100k0ms
export DELAY=0
export BENCH_NAME=rate100k0ms
# export JVMARGS="-agentlib:jdwp=transport=dt_socket,address=localhost:5005,server=y,suspend=y -Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:-UseGCOverheadLimit -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms1G -Xmx1G -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=${SERVER_TECH}.${BENCH_NAME}.jfr"
export JVMARGS="-Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:-UseGCOverheadLimit -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms1G -Xmx1G -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=${SERVER_TECH}.${BENCH_NAME}.jfr"
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar ${SERVER_TECH}/build/libs/${SERVER_TECH}-fatcap.jar -d ${DELAY} > ${SERVER_TECH}.${BENCH_NAME}.slog 2>&1 &
tail -f ${SERVER_TECH}.${BENCH_NAME}.slog # CTRL-C

# LOADGEN: rate100k0ms warmup
export CLIENT_TECH=jbender-okhttp-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -c 100000 -n 1000 -w 0 -cmpi 250
# rate100k0ms
export CLIENT_TECH=jbender-okhttp-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -c 100000 -r 100000 -cmpi 250 -w 0 > ${SERVER_TECH}.rate100k0ms.clog 2>&1 &
tail -f ${SERVER_TECH}.rate100k0ms.clog # CTRL-C
```
