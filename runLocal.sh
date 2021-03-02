set -e

export IDAM_URI='http://localhost:5000'
export NG_COMMAND='ng serve --host 0.0.0.0 --configuration local --proxy-config proxy.conf.dev.json'
docker-compose -p dev -f docker-compose.yml -f docker-compose.dev.yml up -d -V --build db xui-manage-cases

./functionalTest/wait-for-http-200.sh localhost:5000/health
curl -X POST "http://localhost:5000/testing-support/accounts" -H "accept: */*" -H "Content-Type: application/json" -H "X-XSRF-TOKEN: GYGVfWbs-mg4fPyq4PAJqpJ1YDRMViRd3Sxc" -d "{ \"email\": \"super@gmail.com\", \"forename\": \"John\", \"password\": \"string\", \"roles\": [ \"caseworker\" ], \"surname\": \"Smith\"}"

# Backend toggleable
if [ "$#" -ne 1 ]; then
  ./gradlew java:backend:bootRun -is
fi
