#!/bin/bash
java -Dapp.name=basic-jsp-embed-demo -Dapp.maven.test -jar ../dependency/appboot-1.1.0.jar app.main.class=com.descartes.basicjsp.embed.demo.Launch app.boot.debug "$@"