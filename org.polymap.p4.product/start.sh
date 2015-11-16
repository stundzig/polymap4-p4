#!/bin/bash -v

# Simple start script
#   - set working dir the INSTALLDIR (allow eclipse exe to find runtime)
#   - find Java (hard wired)
#   - find the workspace (hard wired)
#   - define Java param and app params

INSTALLDIR=`dirname $0`
WORKSPACE=~/p4_workspace
PORT=8080
JAVA_HOME=/usr/lib/jvm/java-8-oracle/

cd $INSTALLDIR

export VMARGS='-Djava.awt.headless=true -Xverify:none -server -XX:+TieredCompilation -Xmx512m -XX:NewRatio=4 -XX:+UseG1GC -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=30 -XX:SoftRefLRUPolicyMSPerMB=1000'
export ARGS='-console -consolelog -registryMultiLanguage -statushandler org.polymap.rhei.batik.statusHandler -debug'
export LOGARGS='-Dorg.eclipse.equinox.http.jetty.log.stderr.threshold=info'

echo $WORKSPACE
./eclipse -vm $JAVA_HOME/bin/java $ARGS -vmargs $VMARGS -Dorg.osgi.service.http.port=$PORT $LOGARGS