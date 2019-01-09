#!/usr/bin/env bash

set -ex

function guess() {
    v=$1
    if [[ ${v} == *-SNAPSHOT ]]; then
        echo "${v%%-SNAPSHOT}"
    else
        echo "${v%.*}.$((${v##*.}+1))-SNAPSHOT"
    fi
}

function sedInPlace() {
	if [ $(uname) = "Darwin" ]; then
		sed -i '' "$1" $2
	else
		sed -i'' "$1" $2
	fi
}

function quit() {
    echo "ERROR: $*"
    exit 1
}