#!/bin/bash
for filename in result/*.jar; do
	echo $filename
    $DOOP_HOME/bin/doop -a context-insensitive -i $filename --platform java_8 --reflection-high-soundness-mode --lb
    bloxbatch -db $DOOP_HOME/last-analysis -print CallGraphEdge > $filename.txt
    echo "finished $filename"
done