@echo on
set envConfFileName="src/main/resources/localhost_windows/envConf.json"
set targetProjectNames=%1%
echo %envConfFileName%
echo %targetProjectNames%

::必须使用call命令来调用gradle，否则gradle执行完之后，bat脚本会退出
call ./gradlew.bat writeClasspath
setlocal ENABLEDELAYEDEXPANSION
set vidx=0
for /F "tokens=*" %%A in (classpath.txt) do (
    SET /A vidx=!vidx! + 1
    set content!vidx!=%%A
)
echo %content1%
set gcp=%content1%;./build/libs/deploy_system-1.0-SNAPSHOT.jar
echo %gcp%

::在windows上面./gradlew构建，Host上的gradlew、Container中的gradlew会产生锁冲突
::./gradlew --console=plain -Pmain=top_control -Pargs="%envConfFileName%,%targetProjectNames%" run

call ./gradlew.bat clean jar
java -cp "%gcp%" com.andyqu.docker.deploy.script.TopControl %envConfFileName% %targetProjectNames%
