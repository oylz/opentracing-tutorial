#!/bin/bash


rm bin -rf
mkdir bin

function build(){
    JN=$1
    MC=$2
    rm pom.xml -rf
    cp pom_base.xml pom.xml
    sed -i "s/jarname/$JN/g" pom.xml
    sed -i "s/mainclass/$MC/g" pom.xml
    rm target -rf
    mvn package
    cp target/$JN".jar" ./bin/
}

build lesson1 lesson01.exercise 
build lesson2 lesson02.exercise 

build formatter_server lesson03.exercise.Formatter
build publisher_server lesson03.exercise.Publisher




