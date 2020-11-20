#!/bin/bash

set -e
LOG_INFO() {
    local content=${1}
    echo -e "\033[32m ${content}\033[0m"
}
check_basic()
{
# check code format
# bash gradlew verifyGoogleJavaFormat
# build
bash gradlew build --info
}
check_basic
