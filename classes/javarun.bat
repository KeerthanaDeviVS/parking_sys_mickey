
@ECHO OFF
javac -cp C:\Users\devi-pt4391\Downloads\MickeyLite\MickeyLite\lib\* *.java
ECHO Success
jar -cfm C:\Users\devi-pt4391\Downloads\MickeyLite\MickeyLite\lib\parkingsys.jar manifest.txt *.class
PAUSE