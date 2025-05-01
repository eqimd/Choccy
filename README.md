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

Configure the CodeQL environment before use, and it is best to configure the GitHub token in the'settings`, otherwise access may be restricted by the GitHub api."--ram 2048" in "Settings"-"Other"-"CodeQL Additional command line Options" is the maximum memory allowed to be used by CodeQL in MB. Please increase it as appropriate according to the configuration of your system, otherwise it may affect the scanning speed or cause the scanning to fail.

Currently, it supports the Release scan of the GitHub repo, the default branch scan, and the original CodeQL database scan. GitHub will automatically compile the CodeQL database for many repositories (you can use the interface`https://api.github.com/repos /<owner>/<repo>/code-scanning/codeql/databases` view), which will save the steps to configure the local compilation environment and the time to compile the database locally, so the scanning mode gives priority to the original database, with [java-sec-code] (https://github.com/JoyChou93/java-sec-code ) Take this project as an example：

![image-20231120221439543](./README/image-20231120221439543.png)

The query kit is a collection of a series of query statements. You can click on the `Query kit` in the menu bar to view and edit. Here you can first select the preset `java_security.qls`.

Then click `Join the Scan queue` on the right side of the project, which will immediately create a task to perform version detection and scanning of the project.

![image-20231120221735521](./README/image-20231120221735521.png)

In the 'task' tab, you can see the current task execution status and execution log：

![image-20231120222224164](./README/image-20231120222224164.png)

After the execution is complete, you can see the specific scan results in the `Analysis Results'tab. The display of the results completely reproduces the display effect of the CodeQL plug-in in vscode, and some optimizations have been made. It contains vulnerability information, vulnerability location, corresponding rule ID, complete vulnerability call link, vulnerability context code, click the hyperlink to jump to the corresponding code location in the GitHub repo.

![image-20231120222331368](./README/image-20231120222331368.png)



After the project is added, the latest version will be pulled for scanning once a week by default. This can be configured in'settings`-`Others`-'timed scanning Cron expressions`：

![image-20231120224135298](./README/image-20231120224135298.png)

In addition, 'settings' - 'Environment' - 'Environment variables' can configure the proxy when the system accesses GitHub, etc.

![image-20231120224210140](./README/image-20231120224210140.png)

You can put your own query library or query statement in the `Packs` directory. After the placement is complete, you can go to the `Query Package'tab to view it.：

![image-20231120224815724](./README/image-20231120224815724.png)

![image-20231120224832881](./README/image-20231120224832881.png)



The query suite is a collection of a series of CodeQL rules, which can be edited and viewed in the 'Query Suite'tab：

![image-20231120225021487](./README/image-20231120225021487.png)

Refer to the official documentation for grammar: https://docs.github.com/zh/code-security/codeql-cli/using-the-advanced-functionality-of-the-codeql-cli/creating-codeql-query-suites



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

