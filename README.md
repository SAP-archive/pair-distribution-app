# pair-rotation-app

This is a CloudFoundry application which can be used for pair generation. The applicaiton can support teams which do development and operation in pairing mode. Currently, supported use cases are: 
- One team from one company which does development
  - optional operation support
- Multiple teams from different companies developing together 
  - optional operation support
  - optioal support for company specific projects

There are different strategies for pair generation. Currently, supported strategies are:
- For develpment
  - rotate every two days
  - developer with more days in the track rotates out
  - creates new pair combinations based on the pair weight. For each pair combination, a weight is computed which is related to how often they worked together. A higher weight means that a pair combination worked more often together than a pair with smaller weight. Pair generation takes the pairs with the smallest weight.
- In case of multiple companies with company specific projects
  - only developers from the company owning the project are considered
  - the rest of the rules are the same like for development
- For operation
  - rotate every week
  - only developers from the same company are considered
  - the rest of the rules are the same like for development

## REST APIs
* For production use: `/pairs/trello`
* For testing `pairs/test/trello?days=<days-in-the-future`

# Requirements

- Access to maven central repository
- Java 8
- CloudFoundry enviroment with MongoDB service
- [Trello](http://trello.com) account

#### Download an Installation

- Clone this repository locally
- Log in to your CloudFoundry account with the CF CLI
- Deploy the application by executing the `deploy.sh` script. This script will build the application and deploy it to the CF account you logged in in the previous step.


# Configuration 

## Configure Trello access

```
cp deploy/application.properties.template deploy/application.properties
cp deploy/manifest.yml.template deploy/manifest.yml
```
Replace all place holders `<...>` inside. Trello credentials for your account can be generated [here](https://developers.trello.com/get-started/start-building#authenticate). You will need also the Id of your trello board. Use the sandbox provided by trello available [here](https://developers.trello.com/sandbox) to get it. With your API key you can executed samples in the sandbox. Execute the `Get Boards` sample to find out the Id of your board. 

## Trello account preparation
The pair-rotation-application accesses information about developers, tracks and companies via Trello-APIs, uses its history and generates the new pairs for the day which is a Trello list.
* Create a list called `Devs`. 
  * Create a card called `Devs` and add all developers of your team as members of to this card. Rotation app will use this information for pair generation.
  * If operation pair is required create a card called `DevOps: <company-name>`. This will automatically generate an operation pair for the `<company-name>`. 
  * create a card called `New`. This card is for all new developer. The members of this card will not be considered for the operation pair.
  * create a card `<company-name>` and add all developers of the `<company-name>` as members of this card.
* create a list called `Tracks`. 
  * For company specific project create a track wiht card title `<company-name>-<project-name>`
  * Create a card for each track prioritized from the top to the bottom. 
  

## Prepare persistence
Create the MongoDB service instance required for the application. E.g. with follwoing command:
```
cf cs mongodb <service-plan> pairsdb
```

# Limitations

## Trello use
The use of the Trello APIs and Trello service are subject to applicable Trello agreements.

## Pair Generation
Pair generation doesn't consider tracks. This means tracks don't play any role yet during pair generation.
This is a missing feature which can improve the pair generation

# License
Copyright (c) 2017 SAP SE

Except as provided below, this software is licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License.You may obtain a copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
