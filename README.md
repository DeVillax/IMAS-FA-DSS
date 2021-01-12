# IMAS FA-DSS

This repository presents the files to configure the FuzzyAgent Systems for the Multi Agent System course, URV.
 
## Instructions on Windows

1) Download the project
2) On the command line, navigate to the root folder of the files
3) Compile the necessary files with ```javac -cp lib\jade.jar;lib\jFuzzyLogic.jar -d src\output\ src\src\agents\*.java src\src\behaviours\*.java```
4) Execute the user agent ```java -cp lib\jade.jar;lib\jFuzzyLogic.jar;src\output jade.Boot -agents user:agents.UserAgent -gui``` 

Both the ManagerAgent and the FuzzyAgents will be created dynamically. 
