@echo off
rem %~dp0 is the directory of this script
java -Dapp.name=appboot-test -jar %~dp0lib\appboot.jar app.boot.debug %*