# Production: ./start.sh
# Run local as ./start.sh local

if [ -z "$1" ]; then
        profile="default"
else
        profile="default,$1"
fi

echo "Starting server with profile $profile"
mvn clean install && java -jar -Dspring.profiles.active=$profile nube-analytics-server/target/*.jar

