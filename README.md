# JEmpower

Benchmark of the Java web server "hello world" example on async/fiber servers, based loosely on the TechEmpower plaintext benchmarks.


##### Build and run each module

There are modules for several different servers.
Each module has its own pom.xml and all the examples are in the default package for easy running, e.g.:

```
cd utow
mvn clean package dependency:copy-dependencies -DoutputDirectory=target
java -cp "target/*" UtowAsync
```


##### Run Comsat

This build of Comsat servers requires the Quasar javaagent to be running:
`java -cp "target/*" -javaagent:target/quasar-core-0.7.4-jdk8.jar ComsatJetty`


##### build Kilim

`(cd kilim; ./build.sh)`


##### "ulimit -n" tools

The tools directory is useful for starting, stopping and testing the servers at high concurrency.
by default, ubuntu makes setting "ulimit -n" hard.
Add it to your path, then:

```
judo.sh   utow/target          UtowAsync    &
ulim.sh ab -r -k -c 4000 -n 1000000 localhost:9097/hello
jkill.sh
```
Another way to run the same server is
`ulim.sh $JAVA_HOME/bin/java -cp 'utow/target/\*' -Xmx1G UtowAsync`

command | description
-------|-------
`judo.sh` | ulimit -n wrapper for java $1/* $@
`jkill.sh` | kill -2 all the servers started with `judo.sh`
`ulim.sh` | ulimit -n wrapper for arbitrary commands (need to escape globs)

NB: `jkill.sh` will kill the process group associated with '__judo_helper.sh'.
I'm not aware of any other user of this name, but it's possible. Buyer beware.


##### Run all the servers

```
for ii in comsat jetty spark utow; do
  (cd $ii; mvn clean package dependency:copy-dependencies -DoutputDirectory=target)
done
(cd kilim; ./build.sh)

QUASAR="-javaagent:$PWD/comsat-servlet/target/quasar-core-0.7.4-jdk8.jar"
PATH=$PWD/tools:$PATH

judo.sh  spark/target            SparkHello                             & # 4567
judo.sh  jetty/target            JettyHandler                           & # 9090
judo.sh  jetty/target            JettyAsyncHandler                      & # 9091
judo.sh  jetty/target            JettyAsyncServlet                      & # 9092
judo.sh  kilim/target            KilimHello                             & # 9093
judo.sh   utow/target            UtowSimple                             & # 9094
judo.sh   utow/target            UtowTechem                             & # 9095
judo.sh comsat-servlet/target    $QUASAR ComsatServletJetty             & # 9096
judo.sh   utow/target            UtowAsync                              & # 9097
judo.sh   utow/target            UtowAsync2                             & # 9098
judo.sh comsat-servlet/target    $QUASAR ComsatServletUndertow          & # 9099
judo.sh comsat-servlet/target    $QUASAR ComsatServletTomcat            & # 9100
judo.sh comsat-webactors/target  $QUASAR ComsatWebActorsServletJetty    & # 9101
judo.sh comsat-webactors/target  $QUASAR ComsatWebActorsServletUndertow & # 9102
judo.sh comsat-webactors/target  $QUASAR ComsatWebActorsServletTomcat   & # 9103
judo.sh comsat-servlet/target    $QUASAR ComsatWebActorsUndertowSingle  & # 9104
judo.sh comsat-servlet/target    $QUASAR ComsatWebActorsNettySingle     & # 9105
```


##### Kill all the servers

```
jkill.sh
```

CTRL-C will also kill a server if it's in the foreground.


##### Misc.

There are challenges when running high concurrency programs, like adjusting the number of available file descriptors and ports.
In the tools directory there are some shell scripts that simplify that by using `ulimits`, e.g. `judo.sh` above.

Here are some example settings when running manually:

```
sysctl net.ipv4.ip_local_port_range ="1024 65535"
# To be run manually with ulimit
sudo bash -c "ulimit -n 102400; su $USER -mc '$JAVA_HOME/bin/java -cp utow/target/\* -Xmx1G UtowAsync2'"
sudo bash -c "ulimit -n 102400; su $USER -mc 'ab -r -k -c 4000 -n 500000 localhost:9098/hello'"
```

More help here: http://gwan.com/en_apachebench_httperf.html

`jempower.nb` is a Netbeans project just to simplify editing the files.

Prolly shouldn't have been added, but at this point, easier to just leave it be.
