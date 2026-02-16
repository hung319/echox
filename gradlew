#!/bin/sh

##############################################################################
# Gradle start up script for POSIX
##############################################################################

# Attempt to set APP_HOME
APP_HOME=$( cd "${0%/*}/.." && pwd )

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
darwin=false
msys=false
case "$( uname )" in
  CYGWIN* | MINGW* )  msys=true    ;;
  Darwin* )           darwin=true  ;;
esac

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD=$JAVA_HOME/bin/java
else
    JAVACMD=java
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1
then
    die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi

# Increase the maximum file descriptors if we can.
if ! "$darwin" && ! "$msys" ; then
    case $MAX_FD in
      max*)
        MAX_FD=$( ulimit -H -n ) ||
            warn "Could not query maximum file descriptor limit"
    esac
    case $MAX_FD in
      '' | soft) :;; *)
        ulimit -n "$MAX_FD" ||
            warn "Could not set maximum file descriptor limit to $MAX_FD"
    esac
fi

# Find gradle wrapper JAR
APP_HOME_GRADLE_WRAPPER_JAR=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
if [ ! -r "$APP_HOME_GRADLE_WRAPPER_JAR" ]; then
    die "ERROR: Gradle wrapper JAR not found at $APP_HOME_GRADLE_WRAPPER_JAR"
fi

exec "$JAVACMD" "$@" -jar "$APP_HOME_GRADLE_WRAPPER_JAR"
