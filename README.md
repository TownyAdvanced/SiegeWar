# SiegeWar
*SiegeWar* adds a war system to Towny.
### Features
* ⚔️ **Sieges**: Wars are conducted by means of sieges. A siege occurs when a nation attacks a town.
* 🤖 **Automatic:** Sieges are started by players and automatically managed by the plugin. Daily staff management of war is not required.
* 🏙️ **Minimally Destructive:** During sieges, towns cannot be damaged or stolen from.
* 🚶 **Slow Paced:** Sieges last 3 days, giving defenders a chance to respond to attacks, and also making the system friendly to casual players.
* 🗺️ **Geopolitical:** The whole server is involved, with no opt-outs. Nations and towns always have a *Peaceful* option, where they can become immune to attack, but vulnerable to peaceful occupation.
* ♟️ **Strategic:** The system has many strategic elements (*e.g. deciding when/where/who to attack, organizing army composition/logisitics/movement, and  diplomacy/occupation/peacefulness etc*). This can be great for thoughful/mature playerbases, but for servers which require more simplistic PVP contests, alternative war systems should be considered.
* 🍎 **Non-Toxic:** If you install the recommended [*TownyResources*](https://github.com/TownyAdvanced/TownyResources) plugin, wars become more focused on the strategic acquisition of material resources, instead of the toxic settlement of personal grudges.
### Links
* [Download](https://github.com/TownyAdvanced/SiegeWar/releases)
* [Installation Guide](https://github.com/TownyAdvanced/SiegeWar/wiki/Siege-War-Installation)
* [User Guide](https://github.com/TownyAdvanced/SiegeWar/wiki/Siege-War-User-Guide)
* [FAQ](https://github.com/TownyAdvanced/SiegeWar/wiki/Siege-War-FAQ)
### Development History:
SiegeWar was developed by [Goosius1](https://github.com/Goosius1) over the course of about a year starting in the fall of 2019. [Goosius1](https://github.com/Goosius1) is the maintainer of this repo for the TownyAdvanced org, and will be coding to his own schedule.
 
Originally SiegeWar was a fork of Towny, being built directly into Towny. This was necessary because Towny did not have a sufficient API to allow for the things SiegeWar wanted to do.

This repo is for the stand-alone SiegeWar.jar, the result of roughly 2 months of work separating SiegeWar from Towny. This included:
  - updating/merging the SiegeWar branch of Towny from 0.96.2.8 all the way up to 0.96.5.12,
  - adding in many, many API events to Towny,
  - changing the entire Siege database to operate on Town metadata.

If you're after the old Towny-with-Siege-War-built-in, you can find the last version [LlmDl](https://github.com/LlmDl) made [here](https://github.com/TownyAdvanced/Towny/files/5766604/Towny-SeigeWar-0.96.5.12-SNAPSHOT%2B512690a.jar.zip). 

Going forward development is occuring solely on the stand-alone plugin, which can be run alongside any modern version of Towny.
