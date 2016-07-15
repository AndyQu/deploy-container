#!/bin/bash
#在Mac本地机器上做测试用

TERM=dumb

projectName=crm

#定制产生配置文件
cp src/main/resources/localhost_mac/envConf.json src/main/resources/envConf.json
./gradlew --console=plain -Pmain=user_input -Pargs="${projectName},src/main/resources/envConf.json" run

#传递到主机上
mkdir -p /tmp/docker-deploy/config/
config_file=/tmp/docker-deploy/config/${projectName}_`date "+%Y-%m-%d_%H-%M-%S"`.json


cp /tmp/${projectName}.json ${config_file}

#执行部署脚本
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_51.jdk/Contents/Home/
PATH=/usr/local/bin/:$PATH
echo $PATH
./gradlew -Pmain=deploy -Pargs=${config_file} run


