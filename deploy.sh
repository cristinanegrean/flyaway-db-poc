#!/bin/bash
#automatic deployment script, takes the desired version of the portal and installs it where this script is installed.
#it takes one parameter which can be LATEST or RELEASE as Nexus Api Requires
#LATEST = takes into consideration SNAPSHOTs when deploying and takes the most recent snapshot
#RELEASE =  takes into consideration only releases
# more in the manual here: https://oss.sonatype.org/nexus-restlet1x-plugin/default/docs/index.html
DATE=$(date +"%Y%m%d%H%M")
MYPATH=/opt/portal
LOGFILE=$MYPATH/bamboo-deploy-$DATE.log

#export PORTAL_CONFIG=$MYPATH/config
#export HIPPO_HOME=$MYPATH/hippo

echo " --> logfile is $LOGFILE"
{
    cd $MYPATH
    echo "--> deploy started"
    #0. Download last wars zip using Nexus rest api
    LAST_WARS=portal-wars-$DATE
    echo "--> downloading $LAST_WARS.zip"
    wget --user=deployment --password=bla "http://bamboo.bla.net/nexus/service/local/artifact/maven/redirect?r=public&g=groupId&a=portal&e=zip&v=$1" --output-document $LAST_WARS.zip
    echo "--> downloading db-update"
    wget --user=deployment --password=bla "http://bamboo.bla.net/nexus/service/local/artifact/maven/redirect?r=public&g=cms&a=db-update&e=jar&v=$1" --output-document db-update.jar

    #1. Stopping hippo
    echo "--> !!stopping hippo”

    
    #2. Execute db-update
    echo "--> executing db-update"
    java -jar $MYPATH/db-update.jar
    #check if update was successful
    if [ $? -eq 0 ]; then
        echo "--> Updating the database ended successfully"
        rm $MYPATH/db-update.jar
    else
        echo "--> Failed to update the db. Process will end"
        rm $MYPATH/db-update.jar
        exit 1
    fi

   
    #5. Starting hippo
    echo "--> !! starting hippo ..."

 } > $LOGFILE