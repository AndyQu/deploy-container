#@IgnoreInspection BashAddShebang
./gradlew clean -x test -x check build
cp build/libs/sromserver-1.0.war $JETTY_HOME/webapps/srcms.war

cd $JETTY_HOME
./bin/jetty.sh start