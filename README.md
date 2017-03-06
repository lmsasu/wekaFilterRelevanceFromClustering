# wekaFilterRelevanceFromClustering
A clustering-based approach is used to compute relevance values (instance weights) for Weka classifiers

To deploy it as a plugin, copy the directory RelevanceFromClustering into wekafiles/packages directory (e.g. C:\users\yourUserName\Wekafiles\packages\RelevanceFromClustering). One can use the filter from Explorer -> Preprocess tab -> Filter -> Unsupervised -> Instance -> RelevanceFromClustering

A deploy script based on ANT can be found in deploy\deploy.bat
