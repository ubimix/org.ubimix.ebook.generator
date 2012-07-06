#!/bin/bash

dir=`pwd`

commons() {
        cd ${dir}/../../../webreformatter.commons/projects
        mvn clean install
}
scrapper() {
        cd ${dir}/../../../webreformatter.scrapper/projects
        mvn clean install
}

books() {
        cd ${dir}/../../../webreformatter.ebook/projects
        mvn clean install -P cli
}

list=$@
if [[ "$1" == "all" ]];
    then 
        list="commons scrapper books"
elif [[ "$1" == "" ]];
    then
        echo "Usage: $N {commons|scrapper|books|all}" >&2
        exit 1
fi; 

for var in $list
do
    case "$var" in
      commons)
        commons
        ;;
      scrapper)
        scrapper
        ;;
      books)
        books
        ;;
      *)
        ;;
    esac
done



