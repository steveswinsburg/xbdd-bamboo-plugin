## XBDD Bamboo Result Sender Plugin
####Installation
Install to your bamboo plugins directory  
Create two global Bamboo variables, `xbdd.username` and `xbdd.passsword` and assign them appropriately  

####Usage
Use the "XBDD Result Sender" task and configure the options appropriately  
This task needs to go in the "finally" section of your job or it may not run on test failures

####Options
#####Host Path
Default: `https://xbdd`  
This should be the URL to your xbdd instance, do not include a trailing slash.

#####Cucumber JSON Report Paths
Default: `target/cukes-report.json`  
This should be a comma seperated strings of the paths to your cucumber JSON files relative to your build directory

#####Product Name
The name of your product on XBDD

#####Version Major
The major component of your product version **1**.2.3

#####Version Minor
The minor component of your product version 1.**2**.3

#####Version Servicepack
The servicepack component of your product version 1.2.**3**

#####Build Number
Default: `${bamboo.buildNumber}`
The build number to be sent to XBDD, normally the default should **not** be changed as it takes the build number straight from bamboo.
