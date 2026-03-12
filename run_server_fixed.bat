@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-17.0.12"
set "Path=%SystemRoot%\System32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SystemRoot%\System32\WindowsPowerShell\v1.0\;C:\Program Files\Java\jdk-17.0.12\bin;%Path%"
call .\mvnw.cmd spring-boot:run
