./gradlew -Pprofile=dev clean build -x test
cp build/libs/srsupplychain-0.1.war $JETTY_HOME/webapps/srsupplychain.war
cd $JETTY_HOME
./bin/jetty.sh start