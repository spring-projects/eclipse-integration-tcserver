# Pivotal tc Server Integration for Eclipse
      
  The Pivotal tc Server Integration for Eclipse adds server adaptors for the Pivotal
  tc Server to the Eclipse JEE tooling. It makes it easy to deploy apps, start, debug
  and stop your tc Server, create new instances with various configurations, all from within Eclipse.

  It also comes with the Spring Dashboard is an optional component, which brings you
  up-to-date information about Spring-related projects as well as an easy-to-use extension
  install to get additional tooling add-ons, like the famous Spring IDE or the Cloud Foundry
  Integration for Eclipse.

## Installation (Release)

  You can install the latest release of the vFabric tc Server Integration for Eclipse from the
  Eclipse Marketplace by looking for "tc Server". You can also install it manually from this update site:

  https://dist.springsource.com/release/TOOLS/eclipse-integration-tcserver/

## Installation (Milestone)

  You can install the latest milestone build of the vFabric tc Server Integration for Eclipse
  manually from this udpate site:

  https://dist.springsource.com/milestone/TOOLS/eclipse-integration-tcserver/

## Installation (CI builds)

  If you want to live on the leading egde, you can also install always up-to-date continuous
  integration buids from this update site:

  https://dist.springsource.com/snapshot/TOOLS/eclipse-integration-tcserver/nightly

  But take care, those builds could be broken from time to time and might contain non-ship-ready
  features that might never appear in the milestone or release builds.

## Questions and bug reports:

  If you have a question that Google can't answer, the best way is to go to the stackoverflow
  using the tag `spring-tool-suite`:

  https://stackoverflow.com/tags/spring-tool-suite[`spring-tool-suite`]
  
  Bug reports and enhancement requests are tracked using GitHub issues here:
  
  https://github.com/spring-projects/eclipse-integration-tcserver/issues

## Developing Pivotal tc Server Integration for Eclipse

  Just clone the repo and import the projects into an Eclipse workspace. The easiest way to ensure
  that your target platform contains all the necessary dependencies, install a CI build into
  your target platform and proceed.

## Building Pivotal tc Server Integration for Eclipse
  
  The Pivotal tc Server Integration for Eclipse project uses Maven Tycho to do continuous integration
  builds and to produce p2 repos and update sites. To build the project yourself, you can execute:

  `mvn -Pe47 -Dmaven.test.skip=true clean install`
