# pair-rotation-app

This is an CF application which can generate pairs based on different strategies. Currently, supported strategies are:
- Two days rotation with context transfer: 
  - rotate every two days
  - developer with more days in the track rotates out
  - creates new pair combinations based on the pair weight. For each pair combination, a weight is computed which is related to how often they worked together. A higher weight means that a pair combination worked more often together than a pair with smaller weight. Pair generation takes the pairs with the smallest weight.
  - uses [trello](http://trello.com) to get information about the available developers for the day and generates the new pairs into trello. 
  - considers development tracks. Pairs are generated for tracks but tracks don't play any role yet during pair generation.
   
## Configure and deploy
#### Configure 
```
cp deploy/application.properties.template deploy/application.properties
cp deploy/manifest.yml.template deploy/manifest.yml
```
Replace all place holders `<...>` inside. Trello credentials for your account can be generated [here](https://developers.trello.com/get-started/start-building#authenticate). You will need also the Id of your trello board. Use the sandbox provided by trello available [here](https://developers.trello.com/sandbox) to get it. With your API key you can executed samples in the sandbox. Execute the `Get Boards` sample to find out the Id of your board. 
#### Prepare persistence
Create the MongoDB service instance required for the application. E.g. with follwoing command:
```
cf cs mongodb v3.0-container pairsdb
```
#### Deploy
After that the application can be deployed by executing the `deploy.sh`. This script will build the application and deploy to your CF account. You must be logged in with the CF CLI.

## Trello use and account preparation
### Trello use
The use of the Trello APIs and Trello service are subject to applicable Trello agreements.

## Trello account preparation
* create a list called `Devs`. 
  * create a card called `Devs` and add all developers of your team as members of this. Rotation app will use this information for pair generation.
  * if you do DevOps create a card called `DevOps: <company-name>`. This will automatically generate an operation pair for the `<company-name>`. This pair stays for one week the same.
  * create a card called `New`. This card is for all new developer. The members of this card will be not considered for the operation pair.
  * create a card `<company-name>` and add all developers of the `<company-name>` as members of this card.
* create a list called `Tracks`. This list should have all tracks as cards prioritized from the top to the bottom. This is also used for pair generation.

## REST APIs
- radom poairs: `/pairs/random`
- OpenStack CPI way: `/pairs/trello`
