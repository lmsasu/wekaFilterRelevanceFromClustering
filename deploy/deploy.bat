set packageName=RelevanceFromClusteringClosestCentroidHighRelevance
rd /S /Q ..\dist\%packageName%
del ..\dist\%packageName%.zip
rd /S /Q "%userprofile%\wekafiles\packages\%packageName%"
call ant -buildfile ..\build_package.xml make_package
xcopy ..\dist\%packageName% "%userprofile%\wekafiles\packages\%packageName%" /I /Q /Y