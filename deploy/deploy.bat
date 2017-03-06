rd /s /q ..\dist\RelevanceFromClusteringClosestCentroidHighRelevance
del ..\dist\RelevanceFromClusteringClosestCentroidHighRelevance.zip
rd /s /q "%userprofile%\wekafiles\packages\RelevanceFromClusteringClosestCentroidHighRelevance"
call ant -buildfile ..\build_package.xml make_package
xcopy /Q /Y ..\dist\RelevanceFromClusteringClosestCentroidHighRelevance "%userprofile%\wekafiles\packages\RelevanceFromClusteringClosestCentroidHighRelevance" /s /i