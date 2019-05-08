#!/usr/bin/env bash

echo "[{" >> /artefactEvaluation/jre.conf
echo "\"version\" : 8," >> /artefactEvaluation/jre.conf
echo "\"path\" : \"/docker-java-home/jre/lib\"" >> /artefactEvaluation/jre.conf
echo "}]" >> /artefactEvaluation/jre.conf