# team-speaker

Team speaker is an app for producing sounds based on events from Gitlab or Jenkins. E.g some Jenkins build has failed, or new merge request created in Gitlab. It uses Acapela or Google TTS to translate text to mp3.

## Usage

Simply clone repo and run

    $ lein run

or using stand alone jar:

    $ lein uberjar
    $ java -Dconfig.location=resources/config.edn -jar target/uberjar/team-speaker-0.1.0-SNAPSHOT-standalone.jar

To convert \*.wav to \*.mp3 in Linux:

    $ avconv -i c4_explode1.wav c4_explode1.mp3

## Options

* config.location - EDN config file to use


## License

Copyright © 2015 Antanas Bastys

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
