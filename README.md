# Choccy
![icon](./web/public/favicon.ico)



## Introduction
Choccy is a project used to monitor updates to the GitHub repo and automatically perform CodeQL analysis scans on it.



## Start

You need to configure your own code compilation environment and CodeQL environment before running：

Download CodeQL binaries and libraries separately https://github.com/github/codeql-cli-binaries/releases, https://github.com/github/codeql/tags

After decompression, it is placed in the same directory as the Choccy binary file, that is,：

```
$ tree . -L 1
.
├── choccy
├── codeql
└── codeql-codeql-cli-v2.19.4
```





The main configuration and functions are in the Web interface, and there are only two command line parameters：

```
-addr string
      Listening address and port (default "0.0.0.0:80")
-token string
      System Token
```

The first time the program runs, it will create a `choccy_data` folder in its own directory to save the data. If you do not specify the token, it will be randomly generated and output to the command line. The project itself has potentially arbitrary command execution and file reading functions, so if the service is open to the public network, please be sure to set a strong password.



## Building

You need to first enter the web directory to compile the front-end, and the front-end resource files will be automatically embedded when compiling the golang back-end.

Simply run `./build.sh`, or alternatively

```shell
cd web
npm install
npm run build
cd ..
go build -o choccy main.go 

#Cross-compile on mac
#CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o choccy_linux_amd64 main.go
#CGO_ENABLED=0 GOOS=windows GOARCH=amd64 go build -o choccy_windows_amd64.exe main.go
#CGO_ENABLED=0 GOOS=darwin GOARCH=amd64 go build -o choccy_darwin_amd64 main.go
```





## Usage example

After building the application, start it and go to address on web browser, and enter system token.

Now you should see the main page
![image-mainpage](./README/image-mainpage.png)

Let's analyze CVE-2023-40826 for package org.pf4j:pf4j. Firstly we need CodeQL query kit.

Create file `choccy_data/suites/java_mine.qls` with the next content:
```
- description: java mine
- queries: .
  from: choccy/java
- include:
    tags contain: mine
```

Next, create CodeQL query with needed tag in file `choccy_data/packs/java/sinks/ZipSlipMine.ql` with the next content:
```
/**
 * @name Arbitrary file access during archive extraction ("Zip Slip")
 * @description Extracting files from a malicious ZIP file, or similar type of archive, without
 *              validating that the destination file path is within the destination directory
 *              can allow an attacker to unexpectedly gain access to resources.
 * @kind path-problem
 * @id java/zipslip
 * @problem.severity error
 * @security-severity 7.5
 * @precision high
 * @tags mine
 */

import java
import semmle.code.java.security.ZipSlipQuery
import ZipSlipFlow::PathGraph

from ZipSlipFlow::PathNode source, ZipSlipFlow::PathNode sink
where ZipSlipFlow::flowPath(source, sink)
select source.getNode(), source, sink,
  "Unsanitized archive entry, which may contain '..', is used in a $@.", sink.getNode(),
  "file system operation"
```

Now check that you have query kit `java_mine.qls` in the "Query Kit" tab
![image-querytab](./README/image-querytab.png)

Click on `java_mine.qls`, the kit should contain our CodeQL query
![image-queryjava](./README/image-queryjava.png)

Now let's add GitHub repository for scanning. Go back to "GitHub Project" tab and click on "+" icon. Fill in the fields

![image-fill](./README/image-fill.png)
![image-github-success](./README/image-github-success.png)

Click on the left button named "Join the scan queue" and go to "Task" tab
![image-task-zero](./README/image-task-zero.png)

Now we see that latest version have no ZipSlip vulnerabilities. Let's add custom built CodeQL database with older version. Supposing you already have the database for version v3.9.0, go to "Database tab" and press "+", and upload database
![image-upload-db](./README/image-upload-db.png)

Then go back to "Task" tab, press "+" and choose "Create from an existing database", and fill the fields
![image-custom-db](./README/image-custom-db.png)

Wait for the scan completion. You should see next
![image-scan-completion](./README/image-scan-completion.png)

Go to "Analysis results" tab and explore the results
![image-results](./README/image-results.png)


### Upload the CodeQL database for scanning

On the 'Database' page, click the plus icon in the upper right corner to package the local database as a zip archive, then upload, wait for the upload and import to complete (this page cannot be closed during the upload)

(The upload function does not use block transmission, so the available memory of the server is required to be greater than the file size)

![image-20231209180915036](./README/image-20231209180915036.png)

![image-20231209181044428](./README/image-20231209181044428.png)

On the 'TASK' page, click the plus icon in the upper right corner, and select the database and query suite. The project name is the same as the database name by default.

![image-20231209175754439](./README/image-20231209175754439.png)


### Select GitHub projects in bulk for scanning

On the 'task' page, select the plus sign icon in the upper right corner and click 'Batch Create from GitHub`

![image-20231221165209187](./README/image-20231221165209187.png)

The 'search statement' is the retrieval of the warehouse. The syntax reference is: https://docs.github.com/en/rest/search/search?apiVersion=2022-11-28#search-repositories

'Scan range' refers to which part is taken out for scanning after being sorted in a certain order.In the screenshot below, it will be sorted in reverse order by the number of stars, and the 11th and 12th warehouses will be scanned.

![image-20231221165319202](./README/image-20231221165319202.png)

