#!/bin/sh


projectRootFolder=`pwd`
envConfFileName=`pwd`"/src/main/resources/envConf.json"
targetProjectNames=$1
./gradlew --console=plain -Pmain=top_control -Pargs="${projectRootFolder},${envConfFileName},${targetProjectNames}" run
