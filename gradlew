#!/bin/sh
exec java -Xmx64m -Xms64m -classpath "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
