#!/bin/sh
mvn clean jacoco:prepare-agent package -P jboss,stage $@
