package com.sankuai.srq.deploy

class InstanceConfig {
    static ProjectMeta srqserver = new ProjectMeta(
            projectName: "srqserver",
            gitRepoUri: "ssh://git@git.sankuai.com/srt/srqserver.git",
            gitbranchName: "dev",
            subModuleBranchName: "dev",

            portList: [
                    ["port": 8088, "description": "http jetty port"] as PortMeta
            ],
            needJavaDebugPort: true,
            logFolder: "/opt/logs/srq/",
            needMountNodeLib: false,
            needMountGradleLib: true,

            deployScriptFile: "srq_deploy.sh",
    )
    static ProjectMeta h5 = new ProjectMeta(
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

    static ProjectMeta crm=new ProjectMeta(
            projectName: "CRM",
            gitRepoUri: "ssh://git@git.sankuai.com/srt/srcrmserver.git",
            gitbranchName: "dev",
            subModuleBranchName: "dev",

            portList: [
                    ["port": 8088, "description": "crm server port"] as PortMeta
            ],
            needJavaDebugPort: false,
            logFolder: "/opt/logs/srq/",
            needMountNodeLib: false,
            needMountGradleLib: true,

            deployScriptFile: "crm_deploy.sh",
    )

    static ProjectMeta jxc=new ProjectMeta(
            projectName: "JXC",
            gitRepoUri: "ssh://git@git.sankuai.com/srt/jxc.git",
            gitbranchName: "dev",

            portList: [
                    ["port": 8088, "description": "jxc server port"] as PortMeta
            ],
            needJavaDebugPort: false,
            logFolder: "/opt/logs/srq/",
            needMountNodeLib: false,
            needMountGradleLib: true,

            deployScriptFile: "jxc_deploy.sh",
    )

    static ProjectMeta fastFood=new ProjectMeta(
            projectName: "Fast-Food",
            gitRepoUri: "ssh://git@git.sankuai.com/srt/srcms.git",
            gitbranchName: "dev",

            portList: [
                    ["port": 8088, "description": "jxc server port"] as PortMeta,
                    ["port": 9103, "description":"thrift port", hostPort:9103] as PortMeta
            ],
            needJavaDebugPort: false,
            logFolder: "/opt/logs/srq/",
            needMountNodeLib: false,
            needMountGradleLib: true,

            deployScriptFile: "srcms_deploy.sh",
    )
	static ProjectMeta srcos=new ProjectMeta(
		projectName: "srcos",                                         //项目名称
		gitRepoUri: "ssh://git@git.sankuai.com/srt/srcosserver.git",        //项目git repo url
		gitbranchName: "dev",                                         //默认branch名称，在部署时还会提示用户输入
		//subModuleBranchName: "",                                       //有子工程则填写，没有则不需要填写
		portList: [//所需要的端口列表
			[
				"port": 8089,                         //端口号
				"description": "http jetty port"    //端口描述
			] as PortMeta
		],
		needJavaDebugPort: true,            //是否需要打开远程调试端口
		logFolder: "/opt/logs/srcos/",      //输入日志的文件夹
		needMountNodeLib: false,            //是否需要加载node.js库（非node工程都填false）
		needMountGradleLib: true,           //是否需要加载gradle库（gradle工程都填true）
		
		deployScriptFile: "srcos_deploy.sh",
	)
	

    def static projectsConfig = [
            'srqserver'   : [srqserver],
            'h5':[h5],
            'crm':[crm],
            'jxc':[jxc],
            'srcms':[fastFood],
            'srqserver_h5': [srqserver, h5],
			'srcos':[srcos]
    ]
}
