#############################################################################################
# This script is used for generation of the release information file                        #
# for the inspectIT homepage and will be called by the Jenkins build manager.               #
# As template, it uses release-info-template.xml.                                           #
# The $VERSION and $BUILD_TYPE variables will be passed by Jenkins.                         #
# For more information on the release information file, see                                 #
# https://inspectit-performance.atlassian.net/wiki/display/DEV/Homepage+Release+description #
#                                                                                           #
# Author: Henning Schulz                                                                    #
#############################################################################################

URL="ftp:\/\/ntftp.novatec-gmbh.de\/inspectit\/releases\/"

AGENT="${URL}${VERSION}\/inspectit-agent.sun1.5-${VERSION}.zip"
CMR_LINUX_32="${URL}${VERSION}\/inspectit-cmr.linux.x86-${VERSION}.tar.gz"
CMR_LINUX_64="${URL}${VERSION}\/inspectit-cmr.linux.x64-${VERSION}.tar.gz"
CMR_WINDOWS_32="${URL}${VERSION}\/inspectit-cmr.windows.x86-${VERSION}.zip"
CMR_WINDOWS_64="${URL}${VERSION}\/inspectit-cmr.windows.x64-${VERSION}.zip"
UI_LINUX_32="${URL}${VERSION}\/inspectit-ui.linux.gtk.x86-${VERSION}.tar.gz"
UI_LINUX_64="${URL}${VERSION}\/inspectit-ui.linux.gtk.x64-${VERSION}.tar.gz"
UI_WINDOWS_32="${URL}${VERSION}\/inspectit-ui.windows.x86-${VERSION}.zip"
UI_WINDOWS_64="${URL}${VERSION}\/inspectit-ui.windows.x64-${VERSION}.zip"
UI_MACOS_32="${URL}${VERSION}\/inspectit-ui.macosx.cocoa.x86-${VERSION}.zip"
UI_MACOS_64="${URL}${VERSION}\/inspectit-ui.macos.cocoa.x64-${VERSION}.zip"
INSTALLER_LINUX_32="${URL}${VERSION}\/inspectit-installer-all.linux.x86-${VERSION}.jar"
INSTALLER_LINUX_64="${URL}${VERSION}\/inspectit-installer-all.linux.x64-${VERSION}.jar"
INSTALLER_WINDOWS_32="${URL}${VERSION}\/inspectit-installer-all.windows.x86-${VERSION}.jar"
INSTALLER_WINDOWS_64="${URL}${VERSION}\/inspectit-installer-all.windows.x64-${VERSION}.jar"

touch release-info-${BUILD_TYPE}.xml
sed -e 's/VERSION/'"${VERSION}"'/g' \
    -e 's/BUILD_TYPE/'"${BUILD_TYPE}"'/g' \
    -e 's/AGENT/'"${AGENT}"'/g' \
    -e 's/CMR_LINUX_32/'"${CMR_LINUX_32}"'/g' \
    -e 's/CMR_LINUX_64/'"${CMR_LINUX_64}"'/g' \
    -e 's/CMR_WINDOWS_32/'"${CMR_WINDOWS_32}"'/g' \
    -e 's/CMR_WINDOWS_64/'"${CMR_WINDOWS_64}"'/g' \
    -e 's/UI_LINUX_32/'"${UI_LINUX_32}"'/g' \
    -e 's/UI_LINUX_64/'"${UI_LINUX_64}"'/g' \
    -e 's/UI_WINDOWS_32/'"${UI_WINDOWS_32}"'/g' \
    -e 's/UI_WINDOWS_64/'"${UI_WINDOWS_64}"'/g' \
    -e 's/UI_MACOS_32/'"${UI_MACOS_32}"'/g' \
    -e 's/UI_MACOS_64/'"${UI_MACOS_64}"'/g' \
    -e 's/INSTALLER_LINUX_32/'"${INSTALLER_LINUX_32}"'/g' \
    -e 's/INSTALLER_LINUX_64/'"${INSTALLER_LINUX_64}"'/g' \
    -e 's/INSTALLER_WINDOWS_32/'"${INSTALLER_WINDOWS_32}"'/g' \
    -e 's/INSTALLER_WINDOWS_64/'"${INSTALLER_WINDOWS_64}"'/g' \
    Commons/resources/scripts/release-info-template.xml > release-info-${BUILD_TYPE}.xml
