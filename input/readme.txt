This directory will contain the input JSON files used by the Sudoku creator program

Note that the JSON input files here have the word "raw" in the name.

These files contain comments (lines starting with "##"). Those comments
must be stripped before the JSON can be read by the Java program.

See the "run" script.


"options_raw.json" contains the driving options for the program. The created
Sudoku puzzle will reflect the settings indicated in this file. The settings
are not entirely compatible with each other (work-in-progress), and
some settings override others. My personal recommendation is to
use the "frequency" patterns for EASY, MEDIUM, and HARD. But it is
possible to set the number of "givens", and ask for rows and/or columns to be
removed, as well as 3x3 boxes to be removed. My experience is that a
generally "random" puzzle is very strange.

"sud_raw.json" contains the raw information used by the Sudoku puzzle
creator, including the seed "solution" matrices. Given the
algorithms, each seed should be the basis for creating millions
(and more) individual puzzles.
