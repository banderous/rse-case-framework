#!/bin/bash
set -eux

wait-for-url() {
    echo "Testing $1"
    timeout -s TERM 45 bash -c \
    'while [[ "$(curl -s -o /dev/null -L -w ''%{http_code}'' ${0})" != "200" ]];\
    do echo "Waiting for ${0}" && sleep 1;\
    done' ${1}
    echo "OK!"
}
wait-for-url http://$1
