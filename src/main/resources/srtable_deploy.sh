./gradlew -Pprofile=dev clean :ops:build -x test
cp ops/build/libs/ops-0.1.war $JETTY_HOME/webapps/srtable-ops.war

cd $JETTY_HOME
./bin/jetty.sh start