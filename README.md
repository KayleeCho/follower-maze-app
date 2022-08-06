## My solution to the challenge
My usual approach to a general refactor:

1. After understanding the code,tried to extract the code that shares the same functionality. 
3. Created an own class for each functionality starting from the model. 
3. Each functionality has written with dependency injection style so that the testing can be done separately. 
4. Tried to have more functional programming style by implementing recursive function calls, lazy evaluation, etc for streaming application. 

## Dead Letter Queue

Used dead letter queues for delivery error and parse error.In production, we could use Kafka as Kafka is reliable and scalable. 
I also added which message was not delivered to whom on Delivery Error. 

## Testing

The tester makes socket connections to the server:

The event source connects on port 9090 and will start sending events as soon as the connection is accepted.
The user clients can connect on port 9099, and communicate with server following events specification and rules outlined in challenge instructions.
Running the tester

## How to run the server + end-to-end tester

### Server

Run `sbt run`

### Run the end-to-end tester
From the project root, run:
100,000 events will be sent to the server
`tester/run3k.sh`
`tester/run100k.sh`

### Unit test
Run `sbt test`


###Original Coding Challenge Instruction
Thanks for trying our development challenge!

With this document you should have received two other files:

followermaze.sh, an executable bash script
FollowerMaze-assembly-2.0.jar, a JAR file to be executed on a JDK 7 JVM
If you haven't received any of these, or if you think there are any problems with the files, please contact us immediately and we will re-send you the missing pieces.

##The Challenge
The challenge proposed here is to build a system which acts as a socket server, reading events from an event source and forwarding them when appropriate to user clients.

Clients will connect through TCP and use the simple protocol described in a section below. There will be two types of clients connecting to your server:

One event source: It will send you a stream of events which may or may not require clients to be notified
Many user clients: Each one representing a specific user, these wait for notifications for events which would be relevant to the user they represent

###The Protocol
The protocol used by the clients is string-based (i.e. a CRLF control character terminates each message). All strings are encoded in UTF-8.

The event source connects on port 9090 and will start sending events as soon as the connection is accepted.

The many user clients will connect on port 9099. As soon as the connection is accepted, they will send to the server the ID of the represented user, so that the server knows which events to inform them of. For example, once connected a user client may send down: 2932\r\n, indicating that they are representing user 2932.

After the identification is sent, the user client starts waiting for events to be sent to them. Events coming from event source should be sent to relevant user clients exactly like read, no modification is required or allowed.

### Events
There are five possible events. The table below describe payloads sent by the event source and what they represent:

Payload	Sequence #	Type	From User Id	To User Id
666|F|60|50	666	Follow	60	50
1|U|12|9	1	Unfollow	12	9
542532|B	542532	Broadcast	-	-
43|P|32|56	43	Private Msg	32	56
634|S|32	634	Status Update	32	-
Using the verification program supplied, you will receive exactly 10000000 events, with sequence number from 1 to 10000000. The events will arrive out of order.

Note: Please do not assume that your code would only handle a finite sequence of events, we expect your server to handle an arbitrarily large events stream (i.e. you would not be able to keep all events in memory or any other storage)

Events may generate notifications for user clients. **If there is a user client ** connected for them, these are the users to be informed for different event types:

Follow: Only the To User Id should be notified
Unfollow: No clients should be notified
Broadcast: All connected user clients should be notified
Private Message: Only the To User Id should be notified
Status Update: All current followers of the From User ID should be notified
If there are no user client connected for a user, any notifications for them must be silently ignored. user clients expect to be notified of events in the correct order, regardless of the order in which the event source sent them.

###The Configuration

During development, it is possible to modify the test program behavior using the following environment variables:

logLevel - Default: info

Modify to "debug" to print debug messages.

eventListenerPort - Default: 9090

The port used by the event source.

clientListenerPort - Default: 9099

The port used to register clients.

totalEvents - Default: 10000000

Number of messages to send.

concurrencyLevel - Default: 100

Number of conected users.

numberOfUsers Default: concurrencyLevel * 10

Total number of users (connected or not)

randomSeed - Default: 666

The seed to generate random values

timeout - Default: 20000

Timeout in milliseconds for clients while waiting for new messages

maxEventSourceBatchSize - Default: 100

The event source flushes messages in random batch sizes and ramdomize the messages order for each batch. For example, if this configuration is "1" the event source will send only ordered messages flushing the connection for each message.

logInterval - Default: 1000

The interval in milliseconds used to log the sent messages counter.

###Your Solution
We expect you to send us the source code of a fully functional server for the proposed challenge using the default configurations. You still might want to stress-test your code with different configuration parameters to make sure it is not too tailored to our test-suite, and is generic enough.

The challenge was designed so that a candidate can implement the solution using just the standard library of most programming languages and environments (i.e. no external gems/JARs/libs/modules/etc.). Feel free to use your preferred build and testing libraries, but the production code should have very minimal dependencies on third-party code, preferably none at all. E.g. we want to see what kinds of networking abstractions you'll come up with for this challenge. Please don't take that assessment criterion away from us by using an external library that does that for you.

Your code should build and run on a Mac or GNU/Linux machine running a recent OS release.

As a non-exhaustive example, we have received successful applications developed on: Node.js, Ruby, JRuby, Haskell, Clojure, Scala, Go, Python, Java, and C/C++.

If you absolutely think you need some sort of third-party library, please write a paragraph to help us better understand your choice.

###Before submitting your code

With this document you received a jar file and a shell script. These contain one possible implementation of the event source and user client described previously.

We expect you to make sure that your solution works with the supplied clients before sending it to us. The first thing we will do with your code is to run it agains these clients, so you can have very early feedback by treating it as a test suite.

To run the clients, first make sure you have the server you wrote running and listening to ports 9090 and 9099, then run:
`tester/run30.sh`
This will start the clients, which will immediately start sending message to your server. You know it finished without errors when it outputs:

 [INFO] ==================================
 [INFO] \o/ ALL NOTIFICATIONS RECEIVED \o/
 [INFO] ==================================


