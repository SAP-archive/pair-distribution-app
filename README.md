# Pair Distribution Application

The `pair-distribution-app` supports teams which do pair programming and operations in pairing mode (DevOps). It is a CloudFoundry application which can generate pair combinations based on different strategies.  Currently, supported use cases for pair programming are: 
* One team from one company which does development
* One team from one company which does development and operations
* Multiple teams from different companies developing together
* Multiple teams from different companies developing together and some of the companies do operations
* Multiple teams from different companies developing together and some of the companies do operations and company specific projects

There are different strategies for pair generation. Currently, supported strategies are:
* For development
  * rotate every day or every two days
  * block rotation for one day
  * the developer with more days in the track rotates out
  * new pair combinations are generated based on the pair weights. Based on its history the `pair-distribution-app` computes for each pair combination, a weight which represents how often a pair worked together and how often a developer from the pair was in a track. Pair generation takes the pair with the smallest weight for a track. In case that more than one combination have the same weight then a random one is choosen.
* For operations
  * rotate in the same way as development or weekly
  * for each company which do operations one operations pair is generated. The operations pairs are developers from the same company and have a card title `<company-name>-ops/interrupt` 
  * the rest of the rules are the same like for development
* In case of multiple companies with company specific projects
  * for each company with company specific projects one pair is generated. The pairs are built with developers from the same company and have a card title `<company-name>-<company-project>`
  * the rest of the rules are the same as for development

### REST APIs
* For two day rotation use: `/pairs/trello`
* For every day rotation use: `/pairs/trello?everyday=true` 
* For testing `pairs/test/trello?days=<days-in-the-future`

# Requirements

- [MVN CLI](https://maven.apache.org/download.cgi#Installation) 
- [Java 8](https://java.com/download/)
- [CloudFoundry](https://www.cloudfoundry.org/) enviroment with MongoDB service
- [CloudFoundry Command Line Interface (CLI)](https://github.com/cloudfoundry/cli) to interact with CloudFoundry
- [Trello](http://trello.com) account

# Download & Installation

* Clone this repository locally
* Log in to your CloudFoundry account with the CF CLI
* Build the application and deploy it to CloudFundry. The application configuration has to be completed before deploying it. 
  * Windows:
  ```
  $ cd <project-root-folder>
  
  # build and execute unit tests
  $ mvn clean install
  
  # deploy the application to CloudFoundry
  $ cf push
  ```
  * Mac OS or Linux: 
  ```
  $ cd <project-root-folder>
  # execute the script
  $ ./deploy.sh
  ```
* Trigger the pair generation by executing a get request to `http(s)://<application-root>/pairs/trello`. This call will generate a pair combination in Trello

# Configuration 

### Configure Application and Trello Access

First, you will need to [generate credentials for your Trello account](https://developers.trello.com/get-started/start-building#authenticate). You will need also the Id of your Trello board. Use the [Trello sandbox](https://developers.trello.com/sandbox) to get the Id.  With your API key you can execute samples in the sandbox. Execute the `Get Boards` sample to find out the Id of your board. The Trello `api.key`, `api.token` and `board.id` have to be added to the `application.properties` file.

```
$ cd <project-root-folder>
$ cp deploy/application.properties.template src/main/resources/application.properties
$ cp deploy/manifest.yml.template manifest.yml
```
Edit both files and replace all place holders `<...>` inside.  

### Trello Account Preparation

The `pair-distribution-app` accesses information about developers, tracks and companies via Trello-APIs, uses its history and generates the new pairs for the day which is a Trello list.
* Create a list called `Devs`. 
  * create a card called `Devs` and add all developers of your team as members to this card. The application will use this information for pair generation.
  * if operations pair is required create a card called `DevOps: <company-name>`. This will automatically generate an operations pair for the `<company-name>`. Default behavoir here is to rotate like the dev pairs. For weekly roation change the configuration to `DevOps: <company-name>-weekly`.  
  * create a card called `New`. This card is for all new developer. The members of this card will not be considered for the operations pair.
  * create a card `<company-name>` for each company and add all developers of a `<company-name>` as members of their company card.
* create a list called `Tracks`. 
  * create a card for each track prioritized from the top to the bottom. 
  * for company specific projects create a card with title `<company-name>-<project-name>`
* by adding the red label to the latest card of a pair you can block the rotation for this pair for the next rotation
  

### Prepare Persistence

Create the MongoDB service instance required for the application. E.g. with follwoing command:
```
cf cs mongodb <service-plan> pairsdb
```

# Limitations

### Trello use

The use of the Trello APIs and Trello service are subject to applicable Trello agreements.

# How to obtain support

Please create a [new issue](https://github.com/SAP/pair-distribution-app/issues/new) if you find any problems.

# License

Copyright (c) 2017 SAP SE

Except as provided below, this software is licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License.You may obtain a copy of the License at:

[LICENSE](https://github.com/SAP/pair-distribution-app/blob/master/LICENSE)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

