#!/bin/sh

envConfFileName="src/main/resources/localhost_mac/envConf.json"
targetProjectNames=$1
./gradlew --console=plain -Pmain=top_control -Pargs="${envConfFileName},${targetProjectNames}" run
