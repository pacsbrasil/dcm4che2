#!/bin/bash

DIRNAME=`dirname $0`
cd $DIRNAME/../lib

mkdir -p ../../target/graph
mkdir -p ../../target/dot

javadoc -docletpath UmlGraph.jar -doclet gr.spinellis.umlgraph.doclet.UmlGraph \
  -d ../../target/dot -views -private ../src/java/ServiceMBeans.java
  
for f in ../../target/dot/*.dot
do \
BASENAME=`basename $f .dot`
dot -Tps $f | \
gs -q -r360 -dNOPAUSE -sDEVICE=pnm -sOutputFile=-  - -c quit | \
pnmcrop | \
pnmscale 0.25 | \
ppmtogif > ../../target/graph/$BASENAME.gif
done
