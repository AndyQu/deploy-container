#@IgnoreInspection BashAddShebang
#必须在srqserver工程根目录下面执行
./gradlew clean -x test -x check build
cp build/libs/srqserver-1.0.war $JETTY_HOME/webapps/sup.war

cd $JETTY_HOME
./bin/jetty.sh start