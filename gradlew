#!/bin/sh
APP_HOME=$( cd "${0%/*}" && pwd -P ) || exit
GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    mkdir -p "$APP_HOME/gradle/wrapper"
    wget -q -O "$GRADLE_WRAPPER_JAR" "https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || \
    wget -q -O "$GRADLE_WRAPPER_JAR" "https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
fi

exec "$JAVA_HOME/bin/java" -Xmx64m -Xms64m -classpath "$GRADLE_WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
