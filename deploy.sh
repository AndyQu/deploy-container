#!/bin/bash
projects=(srqserver h5 crm jxc srcms srqserver_h5 srcos srsupplychain fe-paidui-crm)
function help {
    echo "可以部署的项目如下:"
    for project in ${projects[@]}; do
        echo "    "${project}
    done
    echo "例如 ./deploy crm"
    echo ""
}
if [ $# -lt 1 ] || [ $1 == "list" ]; then
    help
    exit 1
fi

projectName=$1
projectExist=0
for project in ${projects[@]}; do
    if [ ${project} == ${projectName} ];then
        projectExist=1
        break
    fi
done

if [ ${projectExist} -ne 1 ];then
    echo "~~~亲,这个项目不存在哦:${projectName}"
    echo ""
    help
    exit 1
fi

host=172.27.2.94

echo "开始部署工程:${projectName}"
TERM=dumb

#定制产生配置文件
folder=`dirname $0`
cd ${folder}
gradle -Pmain=user_input -Pargs=${projectName} run

#传递到云主机上
config_file=${projectName}_`date "+%Y-%m-%d_%H-%M-%S"`.json
scp /tmp/${projectName}.json root@${host}:/tmp/${config_file}
#执行部署脚本
ssh root@${host} "export JAVA_HOME=/usr/java/jdk1.7.0_79;PATH=/opt/soft/gradle-2.4/bin/:$PATH;echo $PATH;cd /root/docker-deploy;git checkout .;git pull;gradle -Pmain=deploy -Pargs=/tmp/${config_file} run"


