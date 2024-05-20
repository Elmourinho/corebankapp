# Core Bank Application

This application is small bank core application, which allows to create accounts and process different transactions on these accounts

## Installation

Change the direction to the project folder and run the command:

```bash
./gradlew bootRun
```

## Usage

- [Swagger](http://localhost:8080/swagger-ui/index.html#/)
- [RabbitMQ](http://localhost:15672/#/)
- PostgreSQL will be available at the port 5433 (just in case)

## General thoughts:

I began by focusing on the tools, which were provided in the task description. Added flyway support for creating initial database schema. Added LockManager class, for handling lock mechanism for transactions. Utilized test containers for integration tests.

## Improvements:
- Currently, balances are added sequentially. Since it required additional configuration with MyBatis, and from business perspective, I assume that, opening accounts with multiple balances simultaneously is uncommon, therefore, I opted to keep it simple
- Some validations (Currency, Transaction Direction) are missing. Since they are provided as enums in my structure, incorrect values will result in a bad request.
- Additional edge case tests could be added to improve robustness

## Performance testing:
I used Apache JMeter for performance testing. On my local machine, it took approximately 1.1 seconds to add 100 accounts and perform one transaction on each.