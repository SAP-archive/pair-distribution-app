# pair-rotation-app

This is an CF application which can generate pairs based on different strategies. Currently, supported strategies are:
- random pairs: 
  - pairs are generated randomly
  - this strategy doesn't consider any past information 
- OpenStack CPI way: 
  - rotate every two days 
  - creates new pair combinations based on the pair weight. For each pair combination, a weight is computed which is related to how often they worked together. A higher weight means that a pair combination worked more often together than a pair with smaller weight. Pair generation takes the pairs with the smallest weight.
  - uses [trello](http://trello.com) to get information about the available developers for the day and generates the new pairs into trello. 
  - considers development tracks. Pairs are generated for tracks but tracks don't play any role yet during pair generation.
   
## Configure and deploy
Create reguired configs by: 
```
cp deploy/application.properties.template deploy/application.properties
cp deploy/manifest.yml.template deploy/manifest.yml
```
Replace all place holders `<...>` inside. Trello credentials for your account can be generated [here](https://developers.trello.com/get-started/start-building#authenticate). After that the application can be deployed by executing the `deploy.sh`. This script will build the application and deploy to your CF account. You must be logged in with the CF CLI.

## Prepare trello account
* create a list called `Devs`. This list should have a card called `Devs` and members of this cards are all developers available for the day. Rotation app will use this information for pair generation.
* create a list called `Tracks`. This list should have all tracks as cards prioritized from the top to the bottom. This is also used for pair generation.

## REST APIs
- radom poairs: `/pairs/random`
- OpenStack CPI way: `/pairs/trello`
