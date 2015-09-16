#@IgnoreInspection BashAddShebang
./gradlew clean -x test -x check build
cp build/libs/jxc-1.0.war $JETTY_HOME/webapps/jxc.war

cd $JETTY_HOME
./bin/jetty.sh start