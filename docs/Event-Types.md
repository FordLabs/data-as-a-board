# Data as a Board Wiki

## Terminology
* **Event** - A piece of data representing a condition at some timestamp. Events are the basic representation of all data passed through Data as a Board. There are specializations of Event, such as JobEvent or HealthEvent, that represent more concrete situations.

* **Publisher** - A source of Events that publishes events to subscribers of Data Board. 

* **Subscriber** - A sink of Events that listens to Data Board for either all or specific events.


## Event Types
* **JobEvent** - An event representing the state of a remotely executed job, such as those on a continuous integration server.

  _Publishers_:
    * JenkinsJobPublisher


* **HealthEvent** - An event representing the state of a running application (UP/DOWN). 

  _Publishers_:
    * SpringHealthPublisher
    * GenericHealthPublisher


* **StatisticsEvent** - An event representing a set of statistics (key/value pairs).

* **FigureEvent** - An event representing a figure (small string or numerical value).

* **PercentageEvent** - An event representing a percentage.

* **ListEvent** - An event representing a list of items or a list of lists

  _Publishers_:
    * RetroQuestActionItemsPublisher

* **WeatherEvent** - An event representing weather data (temperature, conditions)
  
  _Publishers_:
    * WeatherPublisher

* **QuoteEvent** - An event representing a quote (text, author)

  _Publishers_:
    * UpwisePublisher