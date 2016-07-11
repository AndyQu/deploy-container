#!/bin/bash
# 这是一个脆弱的脚步，只能在项目根目录下执行这个脚本。
projectName=h5
pwd
cp src/main/resources/localhost_mac/envConf.json src/main/resources/envConf.json
./gradlew -Pmain=user_input -Pargs="${projectName},src/main/resources/envConf.json" run
