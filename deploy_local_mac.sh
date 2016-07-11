#!/bin/bash

# 在Mac本地机器上做测试用
# 前提，打通 ssh root@localhost（为什么不能直接置换至root用户???）
# 1. 切换至root用户：sudo su root
# 2. 设置root密码：passwd
# ssh root@localhost "cat >> ~/.ssh/authorized_keys" < ~/.ssh/id_rsa.pub


host=localhost


projects=(srqserver h5 crm jxc srcms srqserver_h5 srcos srsupplychain fe-paidui-crm fe-paidui-crm_crm srtable srcms_h5)
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
folder=`dirname $0`
cd ${folder}
cp src/main/resources/localhost_mac/envConf.json src/main/resources/envConf.json
./gradlew --console=plain -Pmain=user_input -Pargs="${projectName},src/main/resources/envConf.json" run

#传递到主机上
config_file=${projectName}_`date "+%Y-%m-%d_%H-%M-%S"`.json
scp /tmp/${projectName}.json root@${host}:/tmp/${config_file}

#执行部署脚本
ssh root@${host} "export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_51.jdk/Contents/Home/;PATH=/usr/local/bin/:$PATH;echo $PATH;cd /root/;git clone https://github.com/AndyQu/deploy_system.git;cd deploy_system;./gradlew -Pmain=deploy -Pargs=/tmp/${config_file} run"


