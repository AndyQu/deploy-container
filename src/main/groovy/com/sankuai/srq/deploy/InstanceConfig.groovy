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
            new ProjectMeta(
                    projectName: "H5",
                    gitRepoUri: "ssh://git@git.sankuai.com/fe/fe-paidui.git",
                    gitbranchName: "test",

                    portList: [
                            ["port": 8080, "description": "node server port"] as PortMeta
                    ],
                    needJavaDebugPort: false,
                    logFolder: "/opt/logs/srq/",
                    needMountNodeLib: true,
                    needMountGradleLib: false,

                    deployScriptFile: "h5_deploy.sh",
            )
    ]
    static List<ProjectMeta> crmserver = [
            new ProjectMeta(

            ),
    ]
    def static projectsConfig = ['srqserver': srqserver]
}
