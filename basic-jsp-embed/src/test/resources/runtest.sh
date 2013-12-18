#!/bin/bash
java -Dapp.name=basic-jsp-embed -Dapp.maven.test -jar ../lib/appboot.jar app.main.class=com.descartes.basicjsp.embed.LaunchWebApp app.boot.debug "$@"