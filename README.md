# bgstats-lichess-import
Import games from Lichess into BG stats. 
It's a bit jank since the custom importer on BG stats is not currently in a great state.

To use it, just update your username and the date you want to fetch games back through in the script and run it.
The Lichess API throttles fetching games to about 20/second, so if you try to fetch a huge amount of games, it will take a long time to run.
Once the script finishes, it will write a JSON file to the specified directory, which you can transfer to your phone and then in BG stats, you can "Import Backup or Play File...". 

It will not match to any existing players and it will create a new Game entry for Chess as well as a new Location for Lichess.
What I did after importing the games was to merge the player created with my username into my real player entry in BG stats and then everything seems to look okay. 
