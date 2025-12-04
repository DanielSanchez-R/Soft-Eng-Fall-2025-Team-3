## Dependencies Used
Java & Server: JDK 11 (compiler used by Ant), Java 8 language level (lambdas like removeIf), Apache Tomcat 7/8/9 (runtime).  
Libraries located in /lib/:  
- javax.servlet-api-3.0.1.jar  
- mysql-connector-java-5.1.49-bin.jar  
- h2-1.4.200.jar  
- javax.mail-1.6.2.jar  
- activation-1.1.1.jar  
- junit-4.13.2.jar  
- hamcrest-core-1.3.jar  
Note: Servlet API may also be provided automatically by Tomcat.

## Prerequisites
- JDK 11 installed at: C:\Program Files\Java\jdk-11.0.16.1  
- Apache Ant installed and accessible in PATH  
- Apache Tomcat 7/8/9 installed  
- All required JAR files placed in the /lib/ folder  
- Working internet connection if email sending is required

## Environment Setup
The Ant build script explicitly uses the JDK 11 compiler, so no system changes are required.  
(Optional) Temporary PATH setup in PowerShell:  
$env:JAVA_HOME = "C:\Program Files\Java\jdk-11.0.16.1"  
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"  
Verify with: java -version and javac -version.

## Project Structure
Fall2025Project/  
├─ src/ (Java source files: controllers, dao, model, util)  
├─ test/ (JUnit tests)  
├─ web/ (JSP files, assets, WEB-INF/web.xml)  
├─ lib/ (Required 3rd-party JARs)  
├─ build/ (Generated compiled classes)  
├─ dist/ (Generated WAR file)  
└─ build.xml (Ant build script)

## Build Instructions ANT (Terminal / Ant)
Navigate to the project directory:  
cd "C:\Users\danie\Desktop\Fall2025 ProjectSprint3\Fall2025Project"  (*Example only) where YOU downloaded the file
Build and package the WAR:  
ant  
Other useful Ant commands:  
ant clean → removes build/ and dist/  
ant compile → compiles Java source  
ant test → runs all JUnit tests  
ant all → clean + compile + test + war  
ant -p → list available Ant targets  
WAR output location: dist/Fall2025Project.war

## Deploying to Tomcat
1. Run ant to build the WAR file.  
2. Copy dist/Fall2025Project.war to: <TOMCAT_HOME>/webapps/  
3. Start Tomcat via: <TOMCAT_HOME>/bin/startup.bat  
4. Access the web application at:  
http://localhost:8080/Fall2025Project/

## Running Tests (use ant to test project runs n ant and ant all to run tests on classes within app)
ant
ant all

## Database Configuration
The DBConnection class configures database connectivity.  
H2 example settings:  
jdbc:h2:~/pizzas505db  
user: sa  
password: (empty)  
jdbc:h2:file:./data/restaurantdb
user: root  
password: no password

## Accessing the H2 Database Console
If H2 console is enabled (`-Dh2.console=true`), access it at:  
http://localhost:8080/Fall2025Project/h2  
Useful SQL queries:  
SELECT * FROM USERS;  
SELECT * FROM STAFF;  
SELECT * FROM CUSTOMERS;  
SELECT * FROM TABLES;  
SELECT * FROM MENUITEM;  
SELECT * FROM ORDERS;  
SELECT * FROM ORDERITEMS;

## Email Configuration (JavaMail)
EmailUtil.java is used for outgoing email functionality such as order confirmations and reservation notifications. Example Gmail SMTP settings:  
Host: smtp.gmail.com  
Port: 587  
TLS enabled  
Authentication required  
Note: Gmail requires an App Password (not your normal Gmail password).

## Main Application Pages (Direct URLs)
Login page: http://localhost:8080/Fall2025Project/login.jsp  
Admin dashboard: http://localhost:8080/Fall2025Project/adminDashboard.jsp  
Staff dashboard: http://localhost:8080/Fall2025Project/staffDashboard.jsp  
Customer dashboard: http://localhost:8080/Fall2025Project/customerDashboard.jsp  
Forgot password: http://localhost:8080/Fall2025Project/forgotStaffPassword.jsp  

## Running Tests
JUnit tests are executed using:  
ant test  
Reports output to: build/test-reports/

## Summary of Commands
ant → build + compile + war  
ant all → clean + compile + test + war  
ant clean → remove build directories  
ant compile → compile Java source  
ant test → run JUnit tests  
To deploy: copy dist/Fall2025Project.war to Tomcat/webapps and navigate to:  
http://localhost:8080/Fall2025Project/


Library Downloads from the following official sources:

activation-1.1.1.jar
Required by JavaMail
https://repo1.maven.org/maven2/com/sun/activation/javax.activation/1.1.1/


javax.mail-1.6.2.jar
Email sending (SMTP)
https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/

javax.servlet-api-3.0.1.jar
Servlet/JSP support
https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/3.0.1/

mysql-connector-java-5.1.49-bin.jar
MySQL JDBC driver
https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/

h2-1.4.200.jar
Embedded H2 database
https://repo1.maven.org/maven2/com/h2database/h2/1.4.200/

junit-4.13.2.jar
Unit testing framework
https://repo1.maven.org/maven2/junit/junit/4.13.2/

hamcrest-core-1.3.jar
Assertion matchers for JUnit
https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/

Apache Tomcat 7.0.109 (Core ZIP Distribution):
https://archive.apache.org/dist/tomcat/tomcat-7/v7.0.109/bin/
