#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Script for Unix
# ----------------------------------------------------------------------------

# Determine the project base directory
PRG="$0"
while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/"$link"
    fi
done
PRGDIR=`dirname "$PRG"`
BASEDIR=`cd "$PRGDIR" && pwd`

# Maven wrapper jar
WRAPPER_JAR="$BASEDIR/.mvn/wrapper/maven-wrapper.jar"

# Run Maven with the correct project directory
exec java -Dmaven.multiModuleProjectDirectory="$BASEDIR" \
    -cp "$WRAPPER_JAR" \
    org.apache.maven.wrapper.MavenWrapperMain "$@"
