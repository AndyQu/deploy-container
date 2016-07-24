#!/bin/sh
envConfFileName="src/main/resources/localhost_ubuntu/envConf.json"
targetProjectNames=$1
./gradlew writeClasspath
./gradlew clean jar

java -cp `cat classpath.txt`:./build/libs/deploy_system-1.0-SNAPSHOT.jar com.sankuai.srq.deploy.script.TopControl ${envConfFileName} ${targetProjectNames}

#./gradlew --console=plain -Pmain=top_control -Pargs="${envConfFileName},${targetProjectNames}" run
#在美团办公云ubuntu机器上使用./gradlew构建，Host上的gradlew、Container中的gradlew会产生锁冲突
