package com.sankuai.srq.deploy

class InstanceConfig {
    static List<ProjectMeta> srqserver = [
            new ProjectMeta(
                    ProjectName: "srqserver",
                    GitRepoUri: "ssh://git@git.sankuai.com/srt/srqserver.git",
                    GitbranchName: "master",
                    SubModuleBranchName: "master",

                    PortList: [
                            ["Port": 8088, "Description": "http jetty port"] as PortMeta
                    ],
                    NeedJavaDebugPort: true,
                    LogFolder: "/opt/logs/srq/",
                    NeedMountNodeLib: false,
                    NeedMountGradleLib: true,

                    DeployScriptFile: "srq_deploy.sh",
            ),
    ]
    def static projectsConfig = ['srqserver': srqserver]
}
