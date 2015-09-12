#!/bin/bash
if [ $# -lt 1 ]; then
    echo "请指定要部署的工程名称. 目前可部署的工程包括:srqserver"
    exit 1
fi
projectName=$1
host=172.27.2.94

echo "开始部署工程:${projectName}"
TERM=dumb

#定制产生配置文件
folder=`dirname $0`
cd ${folder}
gradle -Pmain=user_input -Pargs=${projectName}

#传递到云主机上
config_file=${projectName}_`date "+%Y-%m-%d_%H-%M-%S"`.json
scp /tmp/${projectName}.json root@${host}:/tmp/${config_file}
#执行部署脚本
ssh root@${host} "export JAVA_HOME=/usr/java/jdk1.7.0_79;PATH=/opt/soft/gradle-2.4/bin/:$PATH;echo $PATH;cd /root/docker-deploy;git checkout .;git pull;gradle -Pmain=deploy -Pargs=/tmp/${config_file}"