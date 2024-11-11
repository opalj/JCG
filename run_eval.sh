#!/bin/bash
sbt "project jcg_testcases" "run --rsrcDir /app/jcg_testcases/src/main/resources --lang js" "run --rsrcDir /app/jcg_testcases/src/main/resources --lang python" \
 "project jcg_evaluation" "runMain FingerprintExtractor --language python -i testcasesOutput/python -o results/python" \
 "project jcg_evaluation" "runMain FingerprintExtractor --language js -i testcasesOutput/js -o results/js"