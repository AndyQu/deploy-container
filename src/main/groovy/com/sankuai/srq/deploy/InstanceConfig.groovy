package com.sankuai.srq.deploy

class InstanceConfig {
    static List<ProjectMeta> srqserver = [
            new ProjectMeta(
                    projectName: "srqserver",
                    gitRepoUri: "ssh://git@git.sankuai.com/srt/srqserver.git",
                    gitbranchName: "master",
                    subModuleBranchName: "master",

                    portList: [
                            ["port": 8088, "description": "http jetty port"] as PortMeta
                    ],
                    needJavaDebugPort: true,
                    logFolder: "/opt/logs/srq/",
                    needMountNodeLib: false,
                    needMountGradleLib: true,

                    deployScriptFile: "srq_deploy.sh",
            ),
    ]
    def static projectsConfig = ['srqserver': srqserver]
}
