## Easy Living Backup
This app is part of the Easy Living project.

This service consumes 3 queues on the rabbitMQ message broker per Unit in the system.

The main job of this service is to backup the units and their configuration. And ensure there is no duplicate data in the database.
If a change is added to the database, that change is forwarded to the configurator as well.
