#!/bin/bash
#在Mac本地机器上做测试用

projects=(web_hive_sql srqserver h5 crm jxc srcms srqserver_h5 srcos srsupplychain fe-paidui-crm fe-paidui-crm_crm srtable srcms_h5)
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


echo "开始部署工程:${projectName}"
TERM=dumb

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


