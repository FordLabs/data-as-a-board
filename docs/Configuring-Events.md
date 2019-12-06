# Application.yml configuration file

For a sample application.yml file, see [here](https://github.com/FordLabs/data-as-a-board/blob/master/service/src/main/resources/application-sample.yml)

There are two sections, `event` and `radiator`.  Each is discussed below.

## Event Section

Tell DaaB what events to consume in a declarative way.  Subsections such as `list` and `quote` correspond to event types.  Under each event type, declare events that will be consumed and give the event an identifier.  

This section is only for events that have built in integrations.  DaaB reads this section to integrate with the third party services.  You will need to provide necessary consumption details, like keys/tokens, etc.  For example, to consume the health of our PCF EDC foundation as an event, provide the URL to check:

```
health:
  applications:
    - id: pcfprod
      name: PCF EDC1 (Prod),
      url: https://api.sys.pd01.edc1.cf.ford.com/v2/info
```

Many built-in integrations have example configurations in the sample file.

### Custom Events

DaaB supports publishing custom events for any event type.

To publish custom events, you first need to register the event with DaaB to create an event id and get an event key.  The key prevents unauthorized event submissions to your event's identifier.

#### Event Registration

```
POST
{daab.url}/endpoint/register
{
   "id": <your event id as a string>
}

Response:
{
   "key": <secret event key, tied to event id>
}
```

#### Event Publishing

Now that you have your event id and event key, publish any event by making a rest request:

```
POST
Headers:  "X-Event-Key": <your key>
{daab.url}/endpoint/publish
{
   "id": <your event id as a string>,
   "time": <time as an ISO compliant string>,
   "eventType": <can be QUOTE, LIST, etc - see [here](https://github.ford.com/FordLabs/data-as-a-board/wiki)>,
   "name": <name for the event - this will show up on the radiator>
   <more event type specific properties here, ex. for quote, include "quote" and "author" as strings>
}

Response:
200 OK
```