#!/bin/bash
DIR=`dirname $0`
cd $DIR
# Set this to an absolute path to launch from anywhere
MAINJAR="lib/appboot.jar"
JAVAPAR="-Dapp.name=appboot-test"
java $JAVAPAR -jar $MAINJAR "$@"
cd -
