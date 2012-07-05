#!/bin/bash
# set path to this folder.
home=`dirname "$0"`
# get path to generator jar inside $home folder
cp=$(find $home -name "org.webreformatter.ebooks.generator-*.jar" | sort );

cd $home
libs="${home}/lib"
cp="${libs}/ant-1.7.0.jar:${libs}/ant-launcher-1.7.0.jar:${libs}/commons-codec-1.4.jar:${libs}/commons-collections-3.2.1.jar:${libs}/commons-lang-2.4.jar:${libs}/commons-logging-1.1.1.jar:${libs}/htmlcleaner-2.2.jar:${libs}/httpclient-4.1.1.jar:${libs}/httpclient-cache-4.1.1.jar:${libs}/httpcore-4.1.jar:${libs}/httpmime-4.1.1.jar:${libs}/jdom-1.1.jar:${libs}/junit-3.8.1.jar:${libs}/log4j-1.2.14.jar:${libs}/mime-util-2.1.3.jar:${libs}/org.apache.commons.httpclient-4.1.1.jar:${libs}/org.htmlcleaner-2.2.0.jar:${libs}/org.webreformatter.commons.adapters-1.1.5.jar:${libs}/org.webreformatter.commons.events-1.1.5.jar:${libs}/org.webreformatter.commons.geo-0.1.0.jar:${libs}/org.webreformatter.commons.iterator-1.1.5.jar:${libs}/org.webreformatter.commons.json-1.1.5.jar:${libs}/org.webreformatter.commons.osgi-1.1.5.jar:${libs}/org.webreformatter.commons.strings-1.1.5.jar:${libs}/org.webreformatter.commons.templates-1.0.0-SNAPSHOT.jar:${libs}/org.webreformatter.commons.templates.velocity-1.0.0-SNAPSHOT.jar:${libs}/org.webreformatter.commons.uri-1.1.5.jar:${libs}/org.webreformatter.commons.utils-1.1.5.jar:${libs}/org.webreformatter.commons.xml-1.1.5.jar:${libs}/org.webreformatter.ebook-0.9.0.jar:${libs}/org.webreformatter.ebook.generator-0.9.0.jar:${libs}/org.webreformatter.scrapper.app-0.9.0.jar:${libs}/org.webreformatter.scrapper.protocol-0.9.0.jar:${libs}/org.webreformatter.scrapper.resources-0.9.0.jar:${libs}/org.webreformatter.scrapper.transformer-0.9.0.jar:${libs}/osgi_R4_compendium-1.0.jar:${libs}/osgi_R4_core-1.0.jar:${libs}/servlet-api-2.4.jar:${libs}/slf4j-api-1.5.6.jar:${libs}/slf4j-log4j12-1.5.6.jar:${libs}/velocity-1.7.jar"
java -Xms128m -Xmx512m -cp $cp org.webreformatter.ebook.remote.apps.xwikiepub.XwikiEpubExporter "$@"
