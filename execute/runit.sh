FIL=${1:?Must specify output PDF filename}


JAV=java

# following for FOP code
MAIN=build/fop.jar

# following for FOP code
CP=$CP:$MAIN
CP=$CP:lib/avalon-framework-api-4.3.1.jar
CP=$CP:lib/avalon-framework-impl-4.3.1.jar
CP=$CP:lib/batik-all-1.9.jar
CP=$CP:lib/commons-io-1.3.1.jar
CP=$CP:lib/commons-logging-1.0.4.jar
CP=$CP:lib/fontbox-2.0.4.jar
CP=$CP:lib/serializer-2.7.2.jar
CP=$CP:lib/xalan-2.7.2.jar
CP=$CP:lib/xercesImpl-2.9.1.jar
CP=$CP:lib/xml-apis-1.3.04.jar
CP=$CP:lib/xml-apis-ext-1.3.04.jar
CP=$CP:lib/xmlgraphics-commons-2.2.jar

# following for JSON
CP=$CP:jackson-core-2.8.8.jar
CP=$CP:jackson-databind-2.8.8.jar
CP=$CP:jackson-annotations-2.8.0.jar

# following for sudoku code
CP=$CP:commons-math3-3.4.1.jar
CP=$CP:.

OBJ=Sud

## convert sud_raw.json to sud.json
## this involves removing comments before
## it is read
## (JSON does not have a good commenting system
##


RAWJSON=sud_raw.json
JSON=sud.json
RAWOPTIONS=options_raw.json
OPTIONS=options.json

grep -v  '^##' $RAWJSON >$JSON
grep -v  '^##' $RAWOPTIONS >$OPTIONS


echo $JAV -classpath $CP $OBJ  $FIL
$JAV -classpath $CP $OBJ  $FIL
