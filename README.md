# sudoku
Tired of commercial sudoku, I read the article https://mathwithbaddrawings.com/2017/01/04/1-2-trillion-ways-to-play-the-same-sudoku/ and decided to make my own. This is a Java program that follows the scheme. There are a fixed set of "solutions", but the program mixes things up, so we approach the 1.2 trillion puzzles available from each solution.

Raw input is in 2 JSON files, one for the existing solutions, and one for "configuration" parameters. There are a number of variants that can be tried. My personal favorite is a set of "frequency" lists, from easy to HARD.

From a Java programming point of view, there are 2 objects. The Sud.java program now creates PDF output directly, because it invokes the Apache FOP system. There is a helper object that formats the FOP XML structure used to make the PDF.

This program requires BFOJson (see GitHub) to handle the JSON input files, and Apache FOP to make the PDF output file. Both sets of JAR's must be present for compilation and execution.

BFOJson JAR files should be available here on GitHub. See:

  https://github.com/faceless2/json


