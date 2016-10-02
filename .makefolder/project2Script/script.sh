#!/bin/bash


echo "current location:"
echo $PWD
$pwd = pwd

echo "creating build.sbt in two dir above"
echo "$(cat proj)" >  ../../build.sbt

echo "return to root directory"
cd ../../

echo "compile bin"
sbt universal:packageBin


#rm ../../../build.sbt
#echo "$(cat proj)" >  ../../build.sbt
#cd ../../
#sbt universal:packageBin
#cd $pwd 
#rm ../../build.sbt
