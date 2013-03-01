#!/bin/bash
DIR=`dirname $0`
cd $DIR
# Set this to an absolute path to launch from anywhere
MAINJAR="lib/appboot.jar"
JAVAPAR="-Dapp.name=basic-jsp"
java $JAVAPAR -jar $MAINJAR "$@"
cd -
