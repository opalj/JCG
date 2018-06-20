#!/bin/bash
for filename in result/*.jar; do
    $DOOP_HOME/bin/doop "-a context-insensitive -i $filename --reflection-high-soundness-mode --lb"
    bloxbatch "-dp last-analysis -print CallGraphEdge > $filename.txt"
done