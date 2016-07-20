#!/bin/sh


projectRootFolder=`pwd`
envConfFileName="src/main/resources/localhost_windows/envConf.json"
targetProjectNames=$1
./gradlew --console=plain -Pmain=top_control -Pargs="${projectRootFolder},${envConfFileName},${targetProjectNames}" run
