#!/bin/bashrc

cd target/universal
unzip chatastrophe-client-0.0.1.zip

cd chatastrophe-client-0.0.1
cd bin

echo $PWD
read name
$PWD/chatastrophe-client $name 127.0.0.1:2552
