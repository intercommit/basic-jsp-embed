#!/bin/bash
java -Dapp.name=basic-jsp-embed-demo -Dapp.maven.test -jar ../dependency/appboot.jar app.main.class=com.descartes.basicjsp.embed.demo.Launch app.boot.debug "$@"