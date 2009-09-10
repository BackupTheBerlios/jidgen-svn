#!/bin/bash

echo "Trying to setup the environment..."

CWD=`dirname $0`
cd $CWD

JIDGEN_JAR=`find . -maxdepth 0 -name jidgen*.jar|head -n1`
if [ -z "$JIDGEN_JAR" ]; then

	# put some more effort 
	JIDGEN_JAR=`find . -name jidgen*.jar|head -n1`
	if [ -z "$JIDGEN_JAR" ]; then

		echo "Jidgen archive not found. Exiting."
		exit -1
	fi
fi
echo "Found jidgen archive \"${JIDGEN_JAR}\".";

CP="${CLASSPATH}:${JIDGEN_JAR}"
echo "Set classpath to \"${CP}\"."

echo
echo "Starting jidgen..."
java -cp $CP de.rrze.idmone.utils.jidgen.IdGenerator $@
