@echo off

del .\test-server\plugins\EndRitual*.jar
copy .\build\libs\EndRitual*.jar .\test-server\plugins\
cd .\test-server\
java -Xms2G -Xmx2G -jar paper-1.20.4.jar --nogui