# Courier
Courier is a cloud native, S3-backed SFTP 
server with Webhook support.

Although the MVP will support S3 and Webhooks, 
the goal is to provide pluggable storage and 
notification mechanisms.

## Work in Progress
Courier is under active development. This 
README will be updated when a working build is 
available for testing.

## Motivation
Many enterprise applications, such as ADP, SAP, 
and Workday, are used within companies as a 
source of truth for their data. These 
applications offer automated SFTP uploads to 
synchronize data with other applications. In 
short, applications that want to interface with 
enterprise data need to support SFTP.

Courier aims to tackle the undifferentiated 
heavy lifting of handling SFTP integrations, 
so developers can focus on differentiating 
their product.
