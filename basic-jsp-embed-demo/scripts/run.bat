@echo off
rem %~dp0 is the directory of this script
java -Dapp.name=basic-jsp-embed-demo -jar %~dp0lib\appboot.jar %*
