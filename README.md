Build all with `gradle fatCapsule`.

Load generator env vars (fill):

``` bash
# LOADTARGET SYSTEM

export JVMARGS="-Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms4G -Xmx4G"


# LOADGEN SYSTEM

export JVMARGS="-server -XX:+AggressiveOpts -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms12G -Xmx12G -XX:+UseG1GC -XX:MaxGCPauseMillis=10"

export SERVER=x.x.x.x
export PORT=8000
```

Script snippet for runs (comment/uncomment and repeat):

``` bash
export SERVER_TECH=jetty-handler-async-dispatch
# export SERVER_TECH=jetty-handler-async-queue

# export SERVER_TECH=jetty-handler-sync

# export SERVER_TECH=servlet-async-dispatch-jetty
# export SERVER_TECH=servlet-async-dispatch-tomcat
# export SERVER_TECH=servlet-async-dispatch-undertow

# export SERVER_TECH=servlet-async-fjp-jetty
# export SERVER_TECH=servlet-async-fjp-tomcat
# export SERVER_TECH=servlet-async-fjp-undertow

# export SERVER_TECH=servlet-sync-comsat-jetty
# export SERVER_TECH=servlet-sync-comsat-tomcat
# export SERVER_TECH=servlet-sync-comsat-undertow

# export SERVER_TECH=servlet-sync-jetty
# export SERVER_TECH=servlet-sync-tomcat
# export SERVER_TECH=servlet-sync-undertow

# export SERVER_TECH=spark-handler-sync

# export SERVER_TECH=undertow-handler-async-dispatch
# export SERVER_TECH=undertow-handler-async-queue

# export SERVER_TECH=undertow-handler-sync

# export SERVER_TECH=webactor-native-netty-single
# export SERVER_TECH=webactor-native-netty-per-session
# export SERVER_TECH=webactor-native-undertow-per-session


# PER TECH


# LOADTARGET: concurrency
export DELAY=3600000
export BENCH_NAME=concurrency
# export JVMARGS="-agentlib:jdwp=transport=dt_socket,address=localhost:5005,server=y,suspend=y -Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms4G -Xmx4G -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=${SERVER_TECH}.${BENCH_NAME}.jfr"
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar ${SERVER_TECH}/build/libs/${SERVER_TECH}-fatcap.jar -d ${DELAY} -c monitoring-server-conf.yml >> ${SERVER_TECH}.${BENCH_NAME}.slog 2>&1 &
tail -f ${SERVER_TECH}.${BENCH_NAME}.slog # CTRL-C

# LOADGEN: concurrency
export CLIENT_TECH=jbender-apache-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -z http://${SERVER}:9000/monitor -smsy -c 54000 -n 54000 -rbs 1 -ebs 1 -cmpi 250 -w 0 >> ${SERVER_TECH}.concurrency.clog 2>&1 &
tail -f ${SERVER_TECH}.concurrency.clog # CTRL-C


# LOADTARGET: rate1k1000ms
export DELAY=1000
export BENCH_NAME=rate1k1000ms
# export JVMARGS="-agentlib:jdwp=transport=dt_socket,address=localhost:5005,server=y,suspend=y -Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms4G -Xmx4G -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=${SERVER_TECH}.${BENCH_NAME}.jfr"
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar ${SERVER_TECH}/build/libs/${SERVER_TECH}-fatcap.jar -d ${DELAY} -c monitoring-server-conf.yml >> ${SERVER_TECH}.${BENCH_NAME}.slog 2>&1 &
tail -f ${SERVER_TECH}.${BENCH_NAME}.slog # CTRL-C

# LOADGEN: rate1k1000ms warmup
export CLIENT_TECH=jbender-apache-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -z http://${SERVER}:9000/monitor -smsy -c 100000 -n 1000 -w 0 -cmpi 250 -ss false
# rate1k1000ms
export CLIENT_TECH=jbender-apache-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -z http://${SERVER}:9000/monitor -smsy -c 100000 -r 1000 -cmpi 250 -w 0 -j 10 >> ${SERVER_TECH}.rate1k1000ms.clog 2>&1 &
tail -f ${SERVER_TECH}.rate1k1000ms.clog # CTRL-C


# LOADTARGET: rate10k100ms
export DELAY=100
export BENCH_NAME=rate10k100ms
# export JVMARGS="-agentlib:jdwp=transport=dt_socket,address=localhost:5005,server=y,suspend=y -Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms4G -Xmx4G -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=${SERVER_TECH}.${BENCH_NAME}.jfr"
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar ${SERVER_TECH}/build/libs/${SERVER_TECH}-fatcap.jar -d ${DELAY} -c monitoring-server-conf.yml >> ${SERVER_TECH}.${BENCH_NAME}.slog 2>&1 &
tail -f ${SERVER_TECH}.${BENCH_NAME}.slog # CTRL-C

# LOADGEN: rate10k100ms warmup
export CLIENT_TECH=jbender-apache-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -z http://${SERVER}:9000/monitor -smsy -c 100000 -n 1000 -w 0 -cmpi 250 -ss false
# rate10k100ms
export CLIENT_TECH=jbender-apache-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -z http://${SERVER}:9000/monitor -smsy -c 100000 -r 10000 -cmpi 250 -w 0 -j 10 >> ${SERVER_TECH}.rate10k100ms.clog 2>&1 &
tail -f ${SERVER_TECH}.rate10k100ms.clog # CTRL-C


# LOADTARGET: rate100k0ms
export DELAY=0
export BENCH_NAME=rate100k0ms
# export JVMARGS="-agentlib:jdwp=transport=dt_socket,address=localhost:5005,server=y,suspend=y -Dco.paralleluniverse.fibers.detectRunawayFibers=false -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncExceptions=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableSyncForward=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableJettyAsyncFixes=true -Dco.paralleluniverse.fibers.servlet.FiberHttpServlet.disableTomcatAsyncFixes=true -server -XX:+AggressiveOpts -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -Xms4G -Xmx4G -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=${SERVER_TECH}.${BENCH_NAME}.jfr"
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar ${SERVER_TECH}/build/libs/${SERVER_TECH}-fatcap.jar -d ${DELAY} -c monitoring-server-conf.yml >> ${SERVER_TECH}.${BENCH_NAME}.slog 2>&1 &
tail -f ${SERVER_TECH}.${BENCH_NAME}.slog # CTRL-C

# LOADGEN: rate100k0ms warmup
export CLIENT_TECH=jbender-apache-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -z http://${SERVER}:9000/monitor -smsy -c 100000 -n 1000 -w 0 -cmpi 250 -ss false
# rate100k0ms
export CLIENT_TECH=jbender-okhttp-fiber
java -Dcapsule.jvm.args=${JVMARGS} -Dcapsule.log=verbose -jar $CLIENT_TECH/build/libs/${CLIENT_TECH}-fatcap.jar -u http://${SERVER}:${PORT}/hello -z http://${SERVER}:9000/monitor -smsy -c 100000 -r 100000 -cmpi 250 -w 0 -j 10 >> ${SERVER_TECH}.rate100k0ms.clog 2>&1 &
tail -f ${SERVER_TECH}.rate100k0ms.clog # CTRL-C
```
