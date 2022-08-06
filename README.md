## My solution to the challenge
My usual approach to a general refactor:

1. After understanding the code, I tried to extract the code that shares the same functionality. 
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

## How to run the server + the tester

### Server

Run `sbt run`

### Run the end-to-end tester
From the project root, run:
`tester/run3k.sh`
`tester/run100k.sh` Note, 100,000 events will be sent to the server

### Unit test
Run `sbt test`

