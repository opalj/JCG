#!/usr/bin/env bash
rm /artefactEvaluation/jre.conf
echo "[" >> /artefactEvaluation/jre.conf
echo "{" >> /artefactEvaluation/jre.conf
echo "\"version\" : 8," >> /artefactEvaluation/jre.conf
echo "\"path\" : \"/docker-java-home/jre/lib\"" >> /artefactEvaluation/jre.conf
echo "}," >> /artefactEvaluation/jre.conf
echo "{" >> /artefactEvaluation/jre.conf
echo "\"version\" : 6," >> /artefactEvaluation/jre.conf
echo "\"path\" : \"/doopBenchmarks/doop-benchmarks/JREs/jre1.6.0_30/lib/\"" >> /artefactEvaluation/jre.conf
echo "}," >> /artefactEvaluation/jre.conf
echo "{" >> /artefactEvaluation/jre.conf
echo "\"version\" : 10," >> /artefactEvaluation/jre.conf
echo "\"path\" : \"/docker-java-home/jre/lib\"" >> /artefactEvaluation/jre.conf
echo "}" >> /artefactEvaluation/jre.conf
echo "]" >> /artefactEvaluation/jre.conf