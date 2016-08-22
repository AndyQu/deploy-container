package com.andyqu.docker.deploy

import com.andyqu.OSType

class ConfigUtil {
	def static String getOSFolderName(){
		switch(OSType.queryOSType()){
			case OSType.Windows:
				return "localhost_windows"
			case OSType.Mac:
				return "localhost_mac"
			case OSType.Ubuntu:
				return "localhost_ubuntu"
			case OSType.Other:
				throw new Exception("unsupported operating system:"+OSType.Other.getOsName())
		}
	}
}
