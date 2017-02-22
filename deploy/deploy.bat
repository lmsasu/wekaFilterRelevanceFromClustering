rd /s /q ..\dist\RelevanceFromClustering
del ..\dist\RelevanceFromClustering.zip
rd /s /q "%userprofile%\wekafiles\packages\RelevanceFromClustering"
call ant -buildfile ..\build_package.xml make_package
xcopy /Q /Y ..\dist\RelevanceFromClustering "%userprofile%\wekafiles\packages\RelevanceFromClustering" /s /i