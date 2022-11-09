rm -rf bin/*

mkdir -p bin/classes
mkdir -p bin/jar

find -name "*.java" > bin/out.txt
javac -d bin/classes @bin/out.txt

echo Main-Class: autor.ProjMain > bin/classes/manifest.txt
echo Class-Path: /afs/eos.ncsu.edu/software/oracle12/oracle/product/12.2/client/jdbc/lib/ojdbc8.jar autor >> bin/classes/manifest.txt
echo "" >> bin/classes/manifest.txt

pushd bin/classes
jar cvfm Proj.jar manifest.txt autor/*.class
mv Proj.jar ../jar/
popd

alias build='source build.sh'
alias run='java -jar bin/jar/Proj.jar'

