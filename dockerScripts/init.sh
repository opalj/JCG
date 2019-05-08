#!/usr/bin/env bash

echo "[\n\t{\n" >> /artefactEvaluation/jre.conf
echo "\"version\" : 8,\n" >> /artefactEvaluation/jre.conf
echo "\"path\" : \"docker-java-home/jre/lib\"\n" >> /artefactEvaluation/jre.conf
echo "\t}\n]" >> /artefactEvaluation/jre.conf