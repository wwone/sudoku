#
# execute the Sudoku code
#
# note output file is specified in this script, alter
# to your satisfaction!

FIL=myoutput.pdf

#
# several dependencies:
# 1) FOP must be installed in both the
#    compilation and execution directory
#    see below for references to the "build"
#    and "lib" directories, full of many
#    JAR files
#
# 2) bfojson-1.jar must be present for
#    both compilation and execution
#    This project is on GitHub as BFOJson
#    see...  https://github.com/faceless2/json
#
# 3) Some sudoku code does math operations. We
#    may or may not really want them, but
#    they are present. Sooo,
#        commons-math3-3.4.1.jar
#    is needed
#
#
#


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
CP=$CP:bfojson-1.jar

# following for sudoku code
CP=$CP:commons-math3-3.4.1.jar
CP=$CP:.

OBJ=Sud

JSON=sud.json
OPTIONS=options.json

echo $JAV -classpath $CP $OBJ  $FIL
$JAV -classpath $CP $OBJ  $FIL
