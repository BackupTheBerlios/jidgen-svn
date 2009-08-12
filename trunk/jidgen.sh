#!/bin/bash

echo "Trying to setup the environment..."

CWD=`dirname $0`
JIDGEN_JAR=`ls ${CWD}/jidgen-*.jar`
echo "Found jidgen archive \"${JIDGEN_JAR}\".";

CP="${CLASSPATH}:${JIDGEN_JAR}"
echo "Set classpath to \"${CP}\"."

echo
echo "Starting jidgen..."
java -cp $CP de.rrze.idmone.utils.jidgen.IdGenerator $*
