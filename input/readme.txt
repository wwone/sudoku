This directory will contain the input JSON files used by the Sudoku creator program

Note that the JSON input files here have the word "raw" in the name.

These files contain comments (lines starting with "##"). Those comments
must be stripped before the JSON can be read by the Java program.

See the "run" script.


"options_raw.json" contains the driving options for the program. The created
Sudoku puzzle will reflect the settings indicated in this file.

"sud_raw.json" contains the raw information used by the Sudoku puzzle
creator, including the seed "solution" matrices. Given the
algorithms, each see should be the basis for creating millions
(and more) individual puzzles.
