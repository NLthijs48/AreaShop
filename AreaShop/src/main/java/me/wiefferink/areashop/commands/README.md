# Commands

This module contains all AreaShop commands. 

* `CommandAreaShop` is extended by all other classes, and implements some general functionality and defines which methods should be implemented.
* Other classes implement specific sub-commands, supply their help messages and tab completion.
* `CommandManager` class in the `managers` package handles incoming commands and dispatches them to the classes in this package.
