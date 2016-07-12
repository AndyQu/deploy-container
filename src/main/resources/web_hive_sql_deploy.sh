#@IgnoreInspection BashAddShebang
#必须在srqserver工程根目录下面执行
./gradlew clean build
nohup java -server -jar build/libs/hivesql-0.1.0.jar &1