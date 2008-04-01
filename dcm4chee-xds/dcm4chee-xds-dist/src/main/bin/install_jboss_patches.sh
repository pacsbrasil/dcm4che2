#!/bin/sh
# -------------------------------------------------------------------------
# copy patched JBoss components into DCM4CHEE installation
# -------------------------------------------------------------------------

DIRNAME=`dirname $0`
XDS_HOME="$DIRNAME"/..
XDS_PATCHES="$XDS_HOME"/patches

if [ x$1 = x ]; then
  echo "Usage: $0 <path-to-dcm4chee-installation-directory>"
  exit 1
fi

DCM4CHEE_HOME="$1"
DCM4CHEE_SERV="$DCM4CHEE_HOME"/server/default

if [ ! -f "$DCM4CHEE_HOME"/bin/run.jar ]; then
  echo Could not locate DCM4CHEE in "$DCM4CHEE_HOME"
  exit 1
fi

cp -v -R "$XDS_PATCHES" "$DCM4CHEE_SERV"


echo XDS.b Repository and Source installed!
