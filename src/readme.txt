The required JAVA files are in this directory,
along with a shell script that compiles
them.

Generally, it is best to keep the compilation
and execution directories the SAME. This is
largely dictated by the required structure
of the JAR files for FOP. It is pretty
complex. Otherwise, the BFOJson JAR file
is all that is needed.

There are several JAVA files here:

FOPData.java
   processor that creates much of the PDF
   file structure. The PDF is made by the
   Apache FOP system
   
JsonProperties.java

  A handy encapsulation of a Java Properties
  object, whose source information is in
  a JSON file
  
JsonUtils.java
   Helpful method(s) for handling JSON data
   and making Java objects
   
Sud.java
   Main Sudoku program

TextContent.java

   Handy encapsulation of text information
   from JSON input
